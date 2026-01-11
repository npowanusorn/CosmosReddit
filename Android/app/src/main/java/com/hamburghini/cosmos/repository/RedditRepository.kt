package com.hamburghini.cosmos.repository

import com.hamburghini.cosmos.manager.ProfileManager
import com.hamburghini.cosmos.model.Post
import com.hamburghini.cosmos.model.RedditListingResponse
import com.hamburghini.cosmos.network.RetrofitClient
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedditRepository @Inject constructor(
    private val profileManager: ProfileManager
) {

    private val publicApiService = RetrofitClient.publicRedditApiService

    // Public posts endpoints (no authentication required)
    suspend fun getHotPosts(
        subreddit: String = "all",
        after: String? = null,
        limit: Int = 25
    ): Response<RedditListingResponse<Post>> {
        return try {
            publicApiService.getHotPosts(subreddit, after, limit)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getNewPosts(
        subreddit: String = "all",
        after: String? = null,
        limit: Int = 25
    ): Response<RedditListingResponse<Post>> {
        return try {
            publicApiService.getNewPosts(subreddit, after, limit)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getTopPosts(
        subreddit: String = "all",
        time: String = "day",
        after: String? = null,
        limit: Int = 25
    ): Response<RedditListingResponse<Post>> {
        return try {
            publicApiService.getTopPosts(subreddit, time, after, limit)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getRisingPosts(
        subreddit: String = "all",
        after: String? = null,
        limit: Int = 25
    ): Response<RedditListingResponse<Post>> {
        return try {
            publicApiService.getRisingPosts(subreddit, after, limit)
        } catch (e: Exception) {
            throw e
        }
    }

    // Authenticated posts endpoints (user's personalized feed)
    suspend fun getMyHotPosts(
        after: String? = null,
        limit: Int = 25
    ): Response<RedditListingResponse<Post>> {
        return try {
            val apiService = profileManager.getAuthenticatedApiService()
                ?: throw IllegalStateException("User not logged in")

            apiService.getMyHotPosts(after, limit)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getMyBestPosts(
        after: String? = null,
        limit: Int = 25
    ): Response<RedditListingResponse<Post>> {
        return try {
            val apiService = profileManager.getAuthenticatedApiService()
                ?: throw IllegalStateException("User not logged in")

            apiService.getMyBestPosts(after, limit)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getMyNewPosts(
        after: String? = null,
        limit: Int = 25
    ): Response<RedditListingResponse<Post>> {
        return try {
            val apiService = profileManager.getAuthenticatedApiService()
                ?: throw IllegalStateException("User not logged in")

            apiService.getMyNewPosts(after, limit)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getMyTopPosts(
        after: String? = null,
        limit: Int = 25,
        timeframe: String = "day"
    ): Response<RedditListingResponse<Post>> {
        return try {
            val apiService = profileManager.getAuthenticatedApiService()
                ?: throw IllegalStateException("User not logged in")

            apiService.getMyTopPosts(after, limit, timeframe)
        } catch (e: Exception) {
            throw e
        }
    }

    // Voting functionality (authenticated)
    suspend fun vote(postId: String, direction: Int): Response<okhttp3.ResponseBody> {
        return try {
            val apiService = profileManager.getAuthenticatedApiService()
                ?: throw IllegalStateException("User not logged in")

            apiService.vote(postId, direction)
        } catch (e: Exception) {
            throw e
        }
    }

    // Save/unsave functionality (authenticated)
    suspend fun savePost(postId: String): Response<okhttp3.ResponseBody> {
        return try {
            val apiService = profileManager.getAuthenticatedApiService()
                ?: throw IllegalStateException("User not logged in")

            apiService.save(postId)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun unsavePost(postId: String): Response<okhttp3.ResponseBody> {
        return try {
            val apiService = profileManager.getAuthenticatedApiService()
                ?: throw IllegalStateException("User not logged in")

            apiService.unsave(postId)
        } catch (e: Exception) {
            throw e
        }
    }

    // Helper method to determine if user is logged in
    fun isUserLoggedIn(): Boolean {
        return profileManager.isLoggedIn()
    }

    // Helper method to get current username
    fun getCurrentUsername(): String {
        return profileManager.getDisplayUsername()
    }
}