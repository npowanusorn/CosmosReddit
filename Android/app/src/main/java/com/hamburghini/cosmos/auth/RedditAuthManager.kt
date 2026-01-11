package com.hamburghini.cosmos.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.hamburghini.cosmos.Constants
import com.hamburghini.cosmos.manager.ProfileManager
import com.hamburghini.cosmos.model.AccessTokenResponse
import com.hamburghini.cosmos.model.RedditAccount
import com.hamburghini.cosmos.model.UserInfo
import com.hamburghini.cosmos.network.RedditApiService
import com.hamburghini.cosmos.network.RetrofitClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedditAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileManager: ProfileManager
) {
    companion object {
        private const val TAG = "RedditAuthManager"
        private const val CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
        private const val PREFS_NAME = "reddit_auth_prefs"
        private const val KEY_AUTH_STATE = "current_auth_state"
    }

    private val authPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Start the OAuth flow by opening Reddit's authorization URL in Chrome Custom Tab
     */
    fun startOAuthFlow(activity: Activity) {
        try {
            // Generate and store a random state parameter for CSRF protection
            val authState = generateRandomString(32)
            storeAuthState(authState)

            val authUrl = buildAuthUrl(authState)

            Log.d(TAG, "Starting OAuth flow with state: $authState")
            Log.d(TAG, "Auth URL: $authUrl")

            // Create Chrome Custom Tab with proper styling
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setUrlBarHidingEnabled(true)
                .setStartAnimations(activity, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .setExitAnimations(activity, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .build()

            // Launch the Custom Tab from the Activity context
            customTabsIntent.launchUrl(activity, Uri.parse(authUrl))

            Log.d(TAG, "OAuth flow started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start OAuth flow", e)
            clearAuthState()
            profileManager.handleLoginError("Failed to start login: ${e.message}")
        }
    }

    /**
     * Handle the OAuth callback from the redirect URI
     */
    suspend fun handleAuthCallback(intent: Intent): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                val uri = intent.data
                if (uri == null) {
                    Log.e(TAG, "No URI data in callback intent")
                    clearAuthState()
                    return@withContext AuthResult.Error("Invalid callback - no data received")
                }

                Log.d(TAG, "Handling auth callback: $uri")

                // Check if this is an error callback
                val error = uri.getQueryParameter("error")
                if (error != null) {
                    val errorDescription = uri.getQueryParameter("error_description") ?: "Unknown error"
                    Log.e(TAG, "OAuth error: $error - $errorDescription")
                    clearAuthState()

                    val userFriendlyMessage = when (error) {
                        "access_denied" -> "You cancelled the login process"
                        "unsupported_response_type" -> "App configuration error - please contact support"
                        "invalid_scope" -> "Invalid permissions requested"
                        "server_error" -> "Reddit server error - please try again later"
                        else -> "Authorization failed: $errorDescription"
                    }

                    return@withContext AuthResult.Error(userFriendlyMessage)
                }

                // Get the authorization code
                val code = uri.getQueryParameter("code")
                if (code.isNullOrBlank()) {
                    Log.e(TAG, "No authorization code in callback")
                    clearAuthState()
                    return@withContext AuthResult.Error("No authorization code received")
                }

                // Verify state parameter for CSRF protection
                val returnedState = uri.getQueryParameter("state")
                val storedState = getStoredAuthState()

                Log.d(TAG, "State verification - Returned: $returnedState, Stored: $storedState")

                if (returnedState.isNullOrBlank()) {
                    Log.e(TAG, "No state parameter in callback")
                    clearAuthState()
                    return@withContext AuthResult.Error("Security verification failed - no state parameter")
                }

                if (storedState.isNullOrBlank()) {
                    Log.e(TAG, "No stored state found")
                    clearAuthState()
                    return@withContext AuthResult.Error("Security verification failed - no stored state")
                }

                if (returnedState != storedState) {
                    Log.e(TAG, "State mismatch: expected $storedState, got $returnedState")
                    clearAuthState()
                    return@withContext AuthResult.Error("Security verification failed - state mismatch")
                }

                Log.d(TAG, "State verification successful. Authorization code received: ${code.take(10)}...")

                // Clear stored state after successful verification
                clearAuthState()

                // Exchange code for access token
                exchangeCodeForToken(code)

            } catch (e: Exception) {
                Log.e(TAG, "Error handling auth callback", e)
                clearAuthState()
                AuthResult.Error("Failed to complete authentication: ${e.message}")
            }
        }
    }

    /**
     * Exchange authorization code for access token
     */
    private suspend fun exchangeCodeForToken(code: String): AuthResult {
        return try {
            val basicAuth = "Basic " + Base64.encodeToString(
                "${Constants.REDDIT_CLIENT_ID}:".toByteArray(),
                Base64.NO_WRAP
            )

            Log.d(TAG, "Exchanging code for token...")

            val response = RetrofitClient.publicRedditApiService.getAccessToken(
                grantType = "authorization_code",
                code = code,
                redirectUri = Constants.REDDIT_REDIRECT_URI,
                basicAuth = basicAuth
            )

            if (response.isSuccessful) {
                val tokenResponse = response.body()
                if (tokenResponse != null) {
                    Log.d(TAG, "Token exchange successful")

                    // Get user info with the new access token
                    val userInfo = fetchUserInfo(tokenResponse.accessToken)
                    if (userInfo != null) {

                        // Create RedditAccount
                        val account = RedditAccount(
                            username = userInfo.name,
                            userId = userInfo.id,
                            accessToken = tokenResponse.accessToken,
                            refreshToken = tokenResponse.refreshToken ?: "",
                            iconImg = userInfo.iconImg,
                            isActive = true
                        )

                        Log.d(TAG, "User info fetched for: ${userInfo.name}")
                        AuthResult.Success(account, userInfo)

                    } else {
                        Log.e(TAG, "Failed to fetch user info")
                        AuthResult.Error("Failed to get user information")
                    }
                } else {
                    Log.e(TAG, "Empty token response")
                    AuthResult.Error("Invalid token response")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Token exchange failed: ${response.code()} - $errorBody")

                val userFriendlyMessage = when (response.code()) {
                    400 -> "Invalid authorization code - please try logging in again"
                    401 -> "Authentication failed - please check your app configuration"
                    429 -> "Too many requests - please wait a moment and try again"
                    500, 502, 503 -> "Reddit server error - please try again later"
                    else -> "Token exchange failed (${response.code()})"
                }

                AuthResult.Error(userFriendlyMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during token exchange", e)
            val userFriendlyMessage = when (e) {
                is java.net.UnknownHostException -> "No internet connection - please check your network"
                is java.net.SocketTimeoutException -> "Connection timeout - please try again"
                else -> "Network error during authentication: ${e.message}"
            }
            AuthResult.Error(userFriendlyMessage)
        }
    }

    /**
     * Fetch user information using access token
     */
    private suspend fun fetchUserInfo(accessToken: String): UserInfo? {
        return try {
            val apiService = createAuthenticatedApiService(accessToken)
            val response = apiService.getMe()

            if (response.isSuccessful) {
                val userInfo = response.body()
                Log.d(TAG, "User info fetched successfully: ${userInfo?.name}")
                userInfo
            } else {
                Log.e(TAG, "Failed to fetch user info: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching user info", e)
            null
        }
    }

    /**
     * Refresh access token using refresh token
     */
    suspend fun refreshAccessToken(refreshToken: String): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                val basicAuth = "Basic " + Base64.encodeToString(
                    "${Constants.REDDIT_CLIENT_ID}:".toByteArray(),
                    Base64.NO_WRAP
                )

                Log.d(TAG, "Refreshing access token...")

                val response = RetrofitClient.publicRedditApiService.getAccessToken(
                    grantType = "refresh_token",
                    redirectUri = Constants.REDDIT_REDIRECT_URI,
                    basicAuth = basicAuth,
                    refreshToken = refreshToken
                )

                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    if (tokenResponse != null) {
                        // Get updated user info
                        val userInfo = fetchUserInfo(tokenResponse.accessToken)
                        if (userInfo != null) {
                            val account = RedditAccount(
                                username = userInfo.name,
                                userId = userInfo.id,
                                accessToken = tokenResponse.accessToken,
                                refreshToken = tokenResponse.refreshToken ?: refreshToken,
                                iconImg = userInfo.iconImg,
                                isActive = true
                            )

                            Log.d(TAG, "Token refresh successful")
                            AuthResult.Success(account, userInfo)
                        } else {
                            AuthResult.Error("Failed to get updated user info")
                        }
                    } else {
                        AuthResult.Error("Empty refresh response")
                    }
                } else {
                    Log.e(TAG, "Token refresh failed: ${response.code()}")

                    val userFriendlyMessage = when (response.code()) {
                        400, 401 -> "Session expired - please log in again"
                        429 -> "Too many requests - please wait a moment"
                        500, 502, 503 -> "Reddit server error - please try again later"
                        else -> "Token refresh failed - please log in again"
                    }

                    AuthResult.Error(userFriendlyMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during token refresh", e)
                AuthResult.Error("Failed to refresh token - please log in again")
            }
        }
    }

    /**
     * Create authenticated API service
     */
    private fun createAuthenticatedApiService(accessToken: String): RedditApiService {
        val authInterceptor = okhttp3.Interceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .header("User-Agent", "android:com.hamburghini.cosmos:v1.0 (by /u/YourUsername)")
                .build()
            chain.proceed(newRequest)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://oauth.reddit.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RedditApiService::class.java)
    }

    /**
     * Build the Reddit OAuth authorization URL
     */
    private fun buildAuthUrl(state: String): String {
        return Uri.Builder()
            .scheme("https")
            .authority("www.reddit.com")
            .path("/api/v1/authorize.compact")
            .appendQueryParameter("client_id", Constants.REDDIT_CLIENT_ID)
            .appendQueryParameter("response_type", Constants.REDDIT_RESPONSE_TYPE)
            .appendQueryParameter("state", state)
            .appendQueryParameter("redirect_uri", Constants.REDDIT_REDIRECT_URI)
            .appendQueryParameter("duration", Constants.REDDIT_DURATION)
            .appendQueryParameter("scope", Constants.REDDIT_SCOPE)
            .build()
            .toString()
    }

    /**
     * Generate cryptographically secure random string
     */
    private fun generateRandomString(length: Int): String {
        val random = SecureRandom()
        val result = StringBuilder()

        repeat(length) {
            result.append(CHARSET[random.nextInt(CHARSET.length)])
        }

        return result.toString()
    }

    /**
     * Store auth state securely
     */
    private fun storeAuthState(state: String) {
        authPrefs.edit().putString(KEY_AUTH_STATE, state).apply()
        Log.d(TAG, "Stored auth state: $state")
    }

    /**
     * Get stored auth state
     */
    private fun getStoredAuthState(): String? {
        return authPrefs.getString(KEY_AUTH_STATE, null)
    }

    /**
     * Clear stored auth state
     */
    private fun clearAuthState() {
        authPrefs.edit().remove(KEY_AUTH_STATE).apply()
        Log.d(TAG, "Cleared stored auth state")
    }

    /**
     * Sealed class for authentication results
     */
    sealed class AuthResult {
        data class Success(
            val account: RedditAccount,
            val userInfo: UserInfo
        ) : AuthResult()

        data class Error(val message: String) : AuthResult()
    }
}