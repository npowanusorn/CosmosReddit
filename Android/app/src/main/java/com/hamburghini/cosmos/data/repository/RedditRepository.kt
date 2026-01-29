package com.hamburghini.cosmos.data.repository

import com.hamburghini.cosmos.core.network.RetrofitClient
import com.hamburghini.cosmos.core.util.Logger
import com.hamburghini.cosmos.data.manager.ProfileManager
import com.hamburghini.cosmos.data.manager.SubscriptionCacheManager
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.data.model.RedditListingData
import com.hamburghini.cosmos.data.model.RedditListingResponse
import com.hamburghini.cosmos.data.model.RedditObject
import com.hamburghini.cosmos.data.model.SubredditAbout
import com.hamburghini.cosmos.data.model.SubredditAboutData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _postsState = MutableStateFlow<PostsState>(PostsState.Idle)
    val postsState: StateFlow<PostsState> = _postsState.asStateFlow()

    // ==================== API Service ====================

    // Use RetrofitClient's public API service
    private val publicApiService = RetrofitClient.publicRedditApiService

    // Use RetrofitClient's authenticated API service (token handled automatically)
    private val authenticatedApiService = RetrofitClient.authenticatedRedditApiService

    // ==================== Public Posts Endpoints ====================

    suspend fun getHotPosts(
        loadType: LoadType,
        subreddit: String = "all",
        after: String? = null,
        limit: Int = 25
    ) {
        _postsState.value = PostsState.InitialLoading

        val response = publicApiService.getHotPosts(subreddit, after, limit)
        handlePostsResponse(loadType, response, SortType.HOT)
    }

    suspend fun getNewPosts(
        loadType: LoadType,
        subreddit: String = "all",
        after: String? = null,
        limit: Int = 25
    ) {
        _postsState.value = PostsState.InitialLoading

        val response = publicApiService.getNewPosts(subreddit, after, limit)
        handlePostsResponse(loadType, response, SortType.NEW)
    }

    suspend fun getTopPosts(
        loadType: LoadType,
        subreddit: String = "all",
        time: String = "day",
        after: String? = null,
        limit: Int = 25
    ) {
        _postsState.value = PostsState.InitialLoading

        val response = publicApiService.getTopPosts(subreddit, time, after, limit)
        handlePostsResponse(loadType, response, SortType.TOP)
    }

    suspend fun getRisingPosts(
        loadType: LoadType,
        subreddit: String = "all",
        after: String? = null,
        limit: Int = 25
    ) {
        _postsState.value = PostsState.InitialLoading

        val response = publicApiService.getRisingPosts(subreddit, after, limit)
        handlePostsResponse(loadType, response, SortType.RISING)
    }

    // ==================== Authenticated Posts Endpoints ====================

    suspend fun getMyHotPosts(
        loadType: LoadType,
        limit: Int = 25
    ) {
        if (!profileManager.isLoggedIn()) {
            throw IllegalStateException("User not logged in")
        }

        val response = when (loadType) {
            LoadType.INITIAL -> {
                _postsState.value = PostsState.InitialLoading
                authenticatedApiService.getMyHotPosts(null, limit)
            }
            LoadType.REFRESH -> {
                _postsState.value = PostsState.Refresh(
                    previous = _postsState.value as PostsState.Success
                )
                authenticatedApiService.getMyHotPosts(null, limit)
            }
            LoadType.MORE -> {
                val after = if (_postsState.value is PostsState.Success) {
                    (_postsState.value as PostsState.Success).currentAfter
                } else null

                _postsState.value = PostsState.LoadingMore(
                    previous = _postsState.value as PostsState.Success
                )

                authenticatedApiService.getMyHotPosts(after, limit)
            }
        }
        handlePostsResponse(loadType, response, SortType.HOT)
    }

    suspend fun getMyBestPosts(
        loadType: LoadType,
        limit: Int = 25
    ) {
        if (!profileManager.isLoggedIn()) {
            throw IllegalStateException("User not logged in")
        }

        val after = if (_postsState.value is PostsState.Success) {
            (_postsState.value as PostsState.Success).currentAfter
        } else null

        _postsState.value = PostsState.LoadingMore(
            previous = _postsState.value as PostsState.Success
        )

        val response = authenticatedApiService.getMyBestPosts(after, limit)
        handlePostsResponse(loadType, response, SortType.BEST)
    }

    suspend fun getMyNewPosts(
        loadType: LoadType,
        limit: Int = 25
    ) {
        if (!profileManager.isLoggedIn()) {
            throw IllegalStateException("User not logged in")
        }

        val after = if (_postsState.value is PostsState.Success) {
            (_postsState.value as PostsState.Success).currentAfter
        } else null

        _postsState.value = PostsState.LoadingMore(
            previous = _postsState.value as PostsState.Success
        )

        val response = authenticatedApiService.getMyNewPosts(after, limit)
        handlePostsResponse(loadType, response, SortType.NEW)
    }

    suspend fun getMyTopPosts(
        loadType: LoadType,
        limit: Int = 25,
        timeframe: String = "day"
    ) {
        if (!profileManager.isLoggedIn()) {
            throw IllegalStateException("User not logged in")
        }

        val after = if (_postsState.value is PostsState.Success) {
            (_postsState.value as PostsState.Success).currentAfter
        } else null

        _postsState.value = PostsState.LoadingMore(
            previous = _postsState.value as PostsState.Success
        )

        val response = authenticatedApiService.getMyTopPosts(after, limit, timeframe)
        handlePostsResponse(loadType, response, SortType.TOP)
    }

    private fun handlePostsResponse(
        loadType: LoadType,
        response: Response<RedditListingResponse<Post>>,
        sortType: SortType
    ) {
        try {
            if (response.isSuccessful) {
                val listing = response.body()
                if (listing != null) {
                    val newPosts = listing.data.children.map { it.data }
                    val newMap: Map<String, Post> = newPosts.associateBy { it.name }
                    val previousMap = (_postsState.value as? PostsState.LoadingMore)?.previous?.posts
                    if (loadType == LoadType.MORE) {
                        _postsState.value = PostsState.Success(
                            posts = previousMap?.plus(newMap) ?: newMap,
                            sortType = sortType,
                            currentAfter = listing.data.after
                        )
                    } else {
                        _postsState.value = PostsState.Success(
                            posts = newMap,
                            sortType = sortType,
                            currentAfter = listing.data.after
                        )
                    }
                } else {
                    _postsState.value = PostsState.Error(
                        message = "No data received",
                        previous = _postsState.value as? PostsState.Success
                    )
                }
            } else {
                val errorMessage = when (response.code()) {
                    403 -> "Access denied - check permissions"
                    429 -> "Too many requests - please wait"
                    500, 502, 503 -> "Reddit server error - try again later"
                    else -> "Failed to load posts (${response.code()})"
                }

                _postsState.value = PostsState.Error(
                    message = errorMessage,
                    previous = _postsState.value as? PostsState.Success
                )
            }
        } catch (e: Exception) {
            _postsState.value = PostsState.Error(
                message = e.message ?: "Failed to load posts",
                previous = _postsState.value as? PostsState.Success
            )
        }
    }

    // ==================== Voting Functionality ====================

    suspend fun vote(postId: String, direction: Int): ActionResult {
        if (!profileManager.isLoggedIn()) {
            throw IllegalStateException("User not logged in")
        }

        Logger.d("vote: $postId, $direction")
        val prevLikes = currentLikes(postId)
        updatePost(postId) { post ->
            Logger.d("updating post: ${post.name}")
            post.copy(
                likes = directionToLikes(direction),
                score = post.score + scoreDelta(prevLikes, direction)
            )
        }
        return try {
            val response = authenticatedApiService.vote(postId, direction)
            if (response.isSuccessful) {
                ActionResult.Success
            } else {
                rollbackVote(postId, prevLikes)
                ActionResult.Failure("Vote failure")
            }
        } catch (e: Exception) {
            Logger.e("Failed to vote: ${e.message}")
            rollbackVote(postId, prevLikes)
            ActionResult.Failure("Vote failure: ${e.message}")
        }
    }

    // ==================== Save/Unsave Functionality ====================

    suspend fun savePost(postId: String): ActionResult {
        if (!profileManager.isLoggedIn()) {
            throw IllegalStateException("User not logged in")
        }

        updatePost(postId) { it.copy(saved = true) }
        return try {
            val response = authenticatedApiService.save(postId)
            if (response.isSuccessful) {
                ActionResult.Success
            } else {
                updatePost(postId) { it.copy(saved = false) }
                ActionResult.Failure("Failed to save post")
            }
        } catch (e: Exception) {
            Logger.e("Save post error: ${e.message}")
            updatePost(postId) { it.copy(saved = false) }
            ActionResult.Failure("Failed to save post: ${e.message}")
        }
    }

    suspend fun unsavePost(postId: String): ActionResult {
        if (!profileManager.isLoggedIn()) {
            throw IllegalStateException("User not logged in")
        }

        updatePost(postId) { it.copy(saved = false) }
        return try {
            val response = authenticatedApiService.unsave(postId)
            if (response.isSuccessful) {
                ActionResult.Success
            } else {
                updatePost(postId) { it.copy(saved = true) }
                ActionResult.Failure("Failed to unsave post")
            }
        } catch (e: Exception) {
            Logger.e("Unsave post error: ${e.message}")
            updatePost(postId) { it.copy(saved = true) }
            ActionResult.Failure("Failed to unsave post")
        }
    }

    // ==================== Helper Methods ====================

    fun isUserLoggedIn(): Boolean {
        return profileManager.isLoggedIn()
    }

    fun getCurrentUsername(): String {
        return profileManager.getDisplayUsername()
    }

    private fun updatePost(
        postId: String,
        transform: (Post) -> Post
    ) {
        val current = _postsState.value
        if (current !is PostsState.Success) return

        val updatedMap = current.posts.toMutableMap()
        updatedMap[postId]?.let { targetPost ->
            updatedMap[postId] = transform(targetPost)
        }
        _postsState.value = current.copy(posts = updatedMap)
    }

    private fun rollbackVote(
        postId: String,
        prevLikes: Boolean?
    ) {
        updatePost(postId) { post ->
            val currentLikes = post.likes
            val rollbackDelta =
                scoreDelta(currentLikes, prevDirection(prevLikes))

            post.copy(
                likes = prevLikes,
                score = post.score + rollbackDelta
            )
        }
    }

    private fun prevDirection(prev: Boolean?): Int =
        when (prev) {
            true -> 1
            false -> -1
            null -> 0
        }

    private fun currentLikes(postId: String): Boolean? {
        val state = _postsState.value
        if (state !is PostsState.Success) return null

        return state.posts[postId]?.likes
    }

    private fun directionToLikes(direction: Int): Boolean? =
        when (direction) {
            1 -> true
            -1 -> false
            0 -> null
            else -> null
        }

    private fun scoreDelta(
        prev: Boolean?,
        direction: Int
    ): Int {
        val next = directionToLikes(direction)

        return when (prev) {
            null if next == true -> +1
            null if next == false -> -1
            true if next == null -> -1
            false if next == null -> +1
            true if next == false -> -2
            false if next == true -> +2
            else -> 0
        }
    }

    // ==================== Subscriptions with Caching ====================

    suspend fun getMySubscribedSubreddits(
        after: String? = null,
        limit: Int = 100,
        useCache: Boolean = true,
        saveToCache: Boolean = true
    ): Response<RedditListingResponse<SubredditAboutData>> = withContext(Dispatchers.IO) {
        val username = profileManager.getCurrentAccount()?.username
            ?: throw IllegalStateException("User not logged in")

        // If this is a paginated request, skip cache
        if (after != null) {
            Logger.d( "Pagination request detected, fetching from API")
            return@withContext authenticatedApiService.getMySubscribedSubreddits(after, limit)
        }

        // Try cache first if enabled
        if (useCache) {
            val subscriptionsList = subscriptionCacheManager.loadSubscriptions(username)
            if (subscriptionsList != null) {
                Logger.d( "Loaded ${subscriptionsList.size} subscriptions from cache")

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

            Logger.d( "No cache available for user: $username")
        }

        // Fetch from API - load ALL subscriptions by paginating automatically
        Logger.d( "Fetching all subscriptions from API")
        val allSubreddits = mutableListOf<SubredditAboutData>()
        var currentAfter: String? = null
        var pageCount = 0
        val maxPages = 20

        try {
            do {
                val response = authenticatedApiService.getMySubscribedSubreddits(currentAfter, limit)

                if (response.isSuccessful) {
                    val listing = response.body()
                    if (listing != null) {
                        val subreddits = listing.data.children.map { it.data }
                        allSubreddits.addAll(subreddits)
                        currentAfter = listing.data.after
                        pageCount++

                        Logger.d( "Fetched page $pageCount with ${subreddits.size} subscriptions, total: ${allSubreddits.size}")
                    } else {
                        break
                    }
                } else {
                    return@withContext response
                }
            } while (currentAfter != null && pageCount < maxPages)

            // Save to cache if enabled
            if (saveToCache && allSubreddits.isNotEmpty()) {
                Logger.d( "Saving ${allSubreddits.size} subscriptions to cache")
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
                    after = null,
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
            if (!profileManager.isLoggedIn()) {
                throw IllegalStateException("User not logged in")
            }

            val response = authenticatedApiService.subscribe(subredditName, action)

            // Invalidate cache after subscription change
            if (response.isSuccessful) {
                val username = profileManager.getCurrentAccount()?.username
                if (username != null) {
                    Logger.d( "Clearing cache after subscription change for user: $username")
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

sealed interface PostsState {
    data object Idle : PostsState
    data object InitialLoading : PostsState
    data class LoadingMore(
        val previous: Success
    ) : PostsState
    data class Refresh(
        val previous: Success?
    ) : PostsState
    data class Success(
        val posts: Map<String, Post>,
        val sortType: SortType,
        val currentAfter: String?
    ) : PostsState
    data class Error(
        val message: String,
        val previous: Success? = null
    ) : PostsState
}

sealed interface ActionResult {
    data object Success : ActionResult
    data class Failure(val reason: String) : ActionResult
}

enum class SortType {
    HOT, BEST, NEW, TOP, RISING
}

enum class LoadType {
    INITIAL, REFRESH, MORE
}