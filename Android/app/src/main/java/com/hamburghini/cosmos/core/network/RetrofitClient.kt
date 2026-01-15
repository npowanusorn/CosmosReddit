package com.hamburghini.cosmos.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val PUBLIC_BASE_URL = "https://www.reddit.com/"
//    private const val OAUTH_BASE_URL = "https://oauth.reddit.com/"

    // Interceptor to add Authorization header for authenticated calls
//    private val authInterceptor = Interceptor { chain ->
//        val originalRequest = chain.request()
//        val accessToken = AccountManager.getAccessToken() // Changed from SharedPreferencesManager
//        val newRequest = if (accessToken != null) {
//            originalRequest.newBuilder()
//                .header("Authorization", "Bearer $accessToken")
//                .build()
//        } else {
//            originalRequest
//        }
//        chain.proceed(newRequest)
//    }
//
//    // Authenticator for refreshing tokens (when 401 Unauthorized is received)
//    private val tokenAuthenticator = object : Authenticator {
//        override fun authenticate(route: Route?, response: Response): okhttp3.Request? {
//            if (response.code == 401 && AccountManager.getRefreshToken() != null) {
//                val result = runBlocking {
//                    RedditAuthManager.refreshActiveAccountTokens()
//                }
//
//                return when (result) {
//                    is RedditAuthManager.AuthResult.Success -> {
//                        response.request.newBuilder()
//                            .header("Authorization", "Bearer ${result.account.accessToken}")
//                            .build()
//                    }
//                    is RedditAuthManager.AuthResult.Error -> null
//                }
//            }
//            return null
//        }
//    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttpClient for public API calls (no auth or token refresh)
    private val publicOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // OkHttpClient for authenticated API calls (with auth interceptor and authenticator)
//    private val oauthOkHttpClient: OkHttpClient by lazy {
//        OkHttpClient.Builder()
//            .addInterceptor(loggingInterceptor)
//            .addInterceptor(authInterceptor)
//            .authenticator(tokenAuthenticator) // This handles automatic token refresh
//            .connectTimeout(30, TimeUnit.SECONDS)
//            .readTimeout(30, TimeUnit.SECONDS)
//            .build()
//    }

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
//    val authenticatedRedditApiService: RedditApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl(OAUTH_BASE_URL)
//            .client(oauthOkHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(RedditApiService::class.java)
//    }

    // Function to refresh access token using refresh token
//    private suspend fun refreshAccessToken(): String? {
//        val refreshToken = AccountManager.getRefreshToken()
//        if (refreshToken == null) return null
//
//        val basicAuth = "Basic " + Base64.encodeToString(
//            "${Constants.REDDIT_CLIENT_ID}:".toByteArray(),
//            Base64.NO_WRAP
//        )
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://www.reddit.com/")
//            .client(publicOkHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(RedditApiService::class.java)
//
//        return try {
//            val response = retrofit.getAccessToken(
//                "refresh_token",
//                null,
//                Constants.REDDIT_REDIRECT_URI,
//                basicAuth,
//                refreshToken
//            )
//            if (response.isSuccessful) {
//                val accessTokenResponse = response.body()
//                accessTokenResponse?.let {
//                    // Update tokens for active account
//                    AccountManager.updateTokensForActiveAccount(
//                        it.accessToken,
//                        it.refreshToken ?: refreshToken
//                    )
//                    it.accessToken
//                }
//            } else {
//                null
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
}