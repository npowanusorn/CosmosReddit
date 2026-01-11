package com.hamburghini.cosmos.repository

import com.hamburghini.cosmos.model.Post
import com.hamburghini.cosmos.model.RedditListingResponse
import com.hamburghini.cosmos.network.RetrofitClient
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedditRepository @Inject constructor() {

    private val apiService = RetrofitClient.publicRedditApiService

    suspend fun getHotPosts(
        subreddit: String = "all",
        after: String? = null,
        limit: Int = 25
    ): Response<RedditListingResponse<Post>> {
        return try {
            apiService.getHotPosts(subreddit, after, limit)
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
            apiService.getNewPosts(subreddit, after, limit)
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
            apiService.getTopPosts(subreddit, time, after, limit)
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
            apiService.getRisingPosts(subreddit, after, limit)
        } catch (e: Exception) {
            throw e
        }
    }
}