package com.hamburghini.cosmos.data.repository

import android.util.Log
import com.hamburghini.cosmos.data.manager.ProfileManager
import com.hamburghini.cosmos.data.manager.SubscriptionCacheManager
import com.hamburghini.cosmos.model.Post
import com.hamburghini.cosmos.model.RedditListingData
import com.hamburghini.cosmos.model.RedditListingResponse
import com.hamburghini.cosmos.model.RedditObject
import com.hamburghini.cosmos.model.SubredditAbout
import com.hamburghini.cosmos.model.SubredditAboutData
import com.hamburghini.cosmos.core.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedditRepository @Inject constructor(
    private val profileManager: ProfileManager,
    private val subscriptionCacheManager: SubscriptionCacheManager
) {
    companion object {
        private const val TAG = "RedditRepository"
    }

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
    suspend fun vote(postId: String, direction: Int): Response<ResponseBody> {
        return try {
            val apiService = profileManager.getAuthenticatedApiService()
                ?: throw IllegalStateException("User not logged in")

            apiService.vote(postId, direction)
        } catch (e: Exception) {
            throw e
        }
    }

    // Save/unsave functionality (authenticated)
    suspend fun savePost(postId: String): Response<ResponseBody> {
        return try {
            val apiService = profileManager.getAuthenticatedApiService()
                ?: throw IllegalStateException("User not logged in")

            apiService.save(postId)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun unsavePost(postId: String): Response<ResponseBody> {
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

    /**
     * Get user's subscribed subreddits with integrated caching
     *
     * @param after For pagination when fetching from API (null for first page)
     * @param limit Number of items per page when fetching from API
     * @param useCache If true, attempts to load from cache first (default: true)
     * @param saveToCache If true, saves API results to cache (default: true)
     *
     * @return Response containing listing data with metadata about cache usage in headers
     */
    suspend fun getMySubscribedSubreddits(
        after: String? = null,
        limit: Int = 100,
        useCache: Boolean = true,
        saveToCache: Boolean = true
    ): Response<RedditListingResponse<SubredditAboutData>> = withContext(Dispatchers.IO) {
        val username = profileManager.getCurrentAccount()?.username
            ?: throw IllegalStateException("User not logged in")

        val apiService = profileManager.getAuthenticatedApiService()
            ?: throw IllegalStateException("User not logged in")

        // If this is a paginated request (after != null), skip cache and go straight to API
        if (after != null) {
            Log.d(TAG, "Pagination request detected, fetching from API")
            return@withContext apiService.getMySubscribedSubreddits(after, limit)
        }

        // Try cache first if enabled
        if (useCache) {
            val subscriptionsList = subscriptionCacheManager.loadSubscriptions(username)
            if (subscriptionsList != null) {
                Log.d(TAG, "Loaded ${subscriptionsList.size} subscriptions from cache")

                val listingData = RedditListingResponse(
                    data = RedditListingData(
                        children = subscriptionsList.map { subreddit ->
                            RedditObject(
                                kind = "t5",
                                data = subreddit
                            )
                        },
                        after = null,
                        before = null
                    )
                )

                return@withContext Response.success(listingData)
            }

            Log.d(TAG, "No cache available for user: $username")
        }

        // Fetch from API - load ALL subscriptions by paginating automatically
        Log.d(TAG, "Fetching all subscriptions from API")
        val allSubreddits = mutableListOf<SubredditAboutData>()
        var currentAfter: String? = null
        var pageCount = 0
        val maxPages = 20

        try {
            do {
                val response = apiService.getMySubscribedSubreddits(currentAfter, limit)

                if (response.isSuccessful) {
                    val listing = response.body()
                    if (listing != null) {
                        val subreddits = listing.data.children.map { it.data }
                        allSubreddits.addAll(subreddits)
                        currentAfter = listing.data.after
                        pageCount++

                        Log.d(TAG, "Fetched page $pageCount with ${subreddits.size} subscriptions, total: ${allSubreddits.size}")
                    } else {
                        break
                    }
                } else {
                    // If API fails, return the error response
                    return@withContext response
                }
            } while (currentAfter != null && pageCount < maxPages)

            // Save to cache if enabled
            if (saveToCache && allSubreddits.isNotEmpty()) {
                Log.d(TAG, "Saving ${allSubreddits.size} subscriptions to cache")
                subscriptionCacheManager.saveSubscriptions(username, allSubreddits)
            }

            // Return all data as single response
            val listingData = RedditListingResponse(
                data = RedditListingData(
                    children = allSubreddits.map { subreddit ->
                        RedditObject(
                            kind = "t5",
                            data = subreddit
                        )
                    },
                    after = null, // All data loaded, no more pages
                    before = null
                )
            )

            Response.success(listingData)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getPopularSubreddits(
        after: String? = null,
        limit: Int = 25
    ): Response<RedditListingResponse<SubredditAboutData>> {
        return try {
            publicApiService.getPopularSubreddits(after, limit)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun subscribeToSubreddit(
        subredditName: String,
        action: String // "sub" or "unsub"
    ): Response<ResponseBody> {
        return try {
            val apiService = profileManager.getAuthenticatedApiService()
                ?: throw IllegalStateException("User not logged in")

            val response = apiService.subscribe(subredditName, action)

            // Invalidate cache after subscription change
            if (response.isSuccessful) {
                val username = profileManager.getCurrentAccount()?.username
                if (username != null) {
                    Log.d(TAG, "Clearing cache after subscription change for user: $username")
                    subscriptionCacheManager.clearCache(username)
                }
            }

            response
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun searchSubreddits(
        query: String,
        after: String? = null,
        limit: Int = 25
    ): Response<RedditListingResponse<SubredditAbout>> {
        return try {
            publicApiService.searchSubreddits(query, "relevance", after, limit)
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Clear subscription cache for current user
     */
    suspend fun clearSubscriptionCache(): Boolean {
        val username = profileManager.getCurrentAccount()?.username ?: return false
        return subscriptionCacheManager.clearCache(username)
    }
}