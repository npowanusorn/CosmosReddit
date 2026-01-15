package com.hamburghini.cosmos.core.network

import android.util.Base64
import com.hamburghini.cosmos.core.constants.Constants
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val PUBLIC_BASE_URL = "https://www.reddit.com/"
    private const val OAUTH_BASE_URL = "https://oauth.reddit.com/"

    // Callback for token management
    private var tokenProvider: TokenProvider? = null
    private var tokenRefresher: TokenRefresher? = null

    /**
     * Interface for providing current access token
     */
    interface TokenProvider {
        fun getAccessToken(): String?
    }

    /**
     * Interface for refreshing tokens
     */
    interface TokenRefresher {
        suspend fun refreshToken(): RefreshResult
    }

    sealed class RefreshResult {
        data class Success(val accessToken: String) : RefreshResult()
        object Failure : RefreshResult()
    }

    /**
     * Set the token provider and refresher
     * Should be called during app initialization
     */
    fun setTokenHandlers(provider: TokenProvider, refresher: TokenRefresher) {
        tokenProvider = provider
        tokenRefresher = refresher
    }

    // Logging interceptor for debugging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Interceptor to add Authorization header for authenticated calls
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val accessToken = tokenProvider?.getAccessToken()

        val newRequest = if (accessToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        chain.proceed(newRequest)
    }

    // Authenticator for refreshing tokens when 401 Unauthorized is received
    private val tokenAuthenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            // Avoid infinite loops - if we already tried to refresh, give up
            if (response.request.header("Authorization")?.contains("Bearer") == true &&
                response.priorResponse?.code == 401) {
                return null
            }

            // Only attempt refresh for 401 responses
            if (response.code != 401) {
                return null
            }

            // Attempt to refresh the token
            val refreshResult = runBlocking {
                tokenRefresher?.refreshToken()
            }

            return when (refreshResult) {
                is RefreshResult.Success -> {
                    // Retry the request with new token
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${refreshResult.accessToken}")
                        .build()
                }
                is RefreshResult.Failure, null -> null
            }
        }
    }

    // OkHttpClient for public API calls (no auth)
    private val publicOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // OkHttpClient for authenticated API calls (with auth interceptor and authenticator)
    private val oauthOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Retrofit instance for public Reddit API
    val publicRedditApiService: RedditApiService by lazy {
        Retrofit.Builder()
            .baseUrl(PUBLIC_BASE_URL)
            .client(publicOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RedditApiService::class.java)
    }

    // Retrofit instance for authenticated Reddit API
    val authenticatedRedditApiService: RedditApiService by lazy {
        Retrofit.Builder()
            .baseUrl(OAUTH_BASE_URL)
            .client(oauthOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RedditApiService::class.java)
    }

    /**
     * Manual token refresh helper
     * Used by ProfileManager and RedditAuthManager
     */
    suspend fun refreshAccessToken(refreshToken: String): String? {
        val basicAuth = "Basic " + Base64.encodeToString(
            "${Constants.REDDIT_CLIENT_ID}:".toByteArray(),
            Base64.NO_WRAP
        )

        return try {
            val response = publicRedditApiService.getAccessToken(
                grantType = "refresh_token",
                redirectUri = Constants.REDDIT_REDIRECT_URI,
                basicAuth = basicAuth,
                refreshToken = refreshToken
            )

            if (response.isSuccessful) {
                response.body()?.accessToken
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Clear token handlers (useful for testing or logout)
     */
    fun clearTokenHandlers() {
        tokenProvider = null
        tokenRefresher = null
    }
}