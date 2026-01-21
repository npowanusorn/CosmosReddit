package com.hamburghini.cosmos.core.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.hamburghini.cosmos.core.constants.Constants
import com.hamburghini.cosmos.core.network.RetrofitClient
import com.hamburghini.cosmos.data.manager.ProfileManager
import com.hamburghini.cosmos.data.model.RedditAccount
import com.hamburghini.cosmos.data.model.UserInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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

        // Retry configuration
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val INITIAL_RETRY_DELAY_MS = 1000L
        private const val MAX_RETRY_DELAY_MS = 10000L
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
                .build()

            // Launch the Custom Tab from the Activity context
            customTabsIntent.launchUrl(activity, authUrl.toUri())

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
     * Exchange authorization code for access token with retry logic
     */
    private suspend fun exchangeCodeForToken(code: String): AuthResult {
        return executeWithRetry {
            val basicAuth = "Basic " + Base64.encodeToString(
                "${Constants.REDDIT_CLIENT_ID}:".toByteArray(),
                Base64.NO_WRAP
            )

            Log.d(TAG, "Exchanging code for token...")

            // Use RetrofitClient's public API service
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
        }
    }

    /**
     * Fetch user information using access token
     * Creates a temporary authenticated API service
     */
    private suspend fun fetchUserInfo(accessToken: String): UserInfo? {
        return try {
            // Temporarily set token in RetrofitClient to fetch user info
            val tempTokenProvider = object : RetrofitClient.TokenProvider {
                override fun getAccessToken(): String = accessToken
            }

            val tempRefresher = object : RetrofitClient.TokenRefresher {
                override suspend fun refreshToken(): RetrofitClient.RefreshResult {
                    return RetrofitClient.RefreshResult.Failure
                }
            }

            // Temporarily set token handlers
            RetrofitClient.setTokenHandlers(tempTokenProvider, tempRefresher)

            // Fetch user info
            val response = RetrofitClient.authenticatedRedditApiService.getMe()

            // Restore original token handlers
            RetrofitClient.setTokenHandlers(profileManager, profileManager)

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
            // Restore original token handlers on error
            RetrofitClient.setTokenHandlers(profileManager, profileManager)
            null
        }
    }

    /**
     * Refresh access token using refresh token with retry logic
     */
    suspend fun refreshAccessToken(refreshToken: String): AuthResult {
        return withContext(Dispatchers.IO) {
            executeWithRetry {
                val basicAuth = "Basic " + Base64.encodeToString(
                    "${Constants.REDDIT_CLIENT_ID}:".toByteArray(),
                    Base64.NO_WRAP
                )

                Log.d(TAG, "Refreshing access token...")

                // Use RetrofitClient's public API service for token refresh
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
            }
        }
    }

    /**
     * Execute a network operation with retry logic for transient failures
     */
    private suspend fun <T> executeWithRetry(
        block: suspend () -> T
    ): T {
        var lastException: Exception? = null
        var currentDelay = INITIAL_RETRY_DELAY_MS

        for (attempt in 1..MAX_RETRY_ATTEMPTS) {
            try {
                return block()
            } catch (e: Exception) {
                lastException = e

                // Check if this is a retryable network error
                val isRetryable = when (e) {
                    is ConnectException,
                    is SocketException,
                    is SocketTimeoutException,
                    is UnknownHostException -> true
                    else -> false
                }

                if (!isRetryable || attempt >= MAX_RETRY_ATTEMPTS) {
                    // Non-retryable error or max attempts reached
                    throw e
                }

                Log.w(TAG, "Network error on attempt $attempt/$MAX_RETRY_ATTEMPTS: ${e.message}")
                Log.d(TAG, "Retrying in ${currentDelay}ms...")

                delay(currentDelay)
                currentDelay = (currentDelay * 2).coerceAtMost(MAX_RETRY_DELAY_MS)
            }
        }

        // This shouldn't be reached, but throw the last exception if it is
        throw lastException ?: Exception("Unknown error during retry")
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