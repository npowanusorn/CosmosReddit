package com.hamburghini.cosmos.data.repository

import android.util.Log
import com.hamburghini.cosmos.core.util.Logger
import com.hamburghini.cosmos.data.manager.ProfileManager
import com.hamburghini.cosmos.data.model.Comment
import com.hamburghini.cosmos.data.model.RedditListingResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentsRepository @Inject constructor(
    private val profileManager: ProfileManager
) {

    // In-memory cache of comments by post ID
    private val _commentsCache = MutableStateFlow<Map<String, CommentsState>>(emptyMap())
    val commentsCache: StateFlow<Map<String, CommentsState>> = _commentsCache.asStateFlow()

    // Cache for individual comment trees (for loading more children)
    private val _commentTreeCache = MutableStateFlow<Map<String, Comment>>(emptyMap())
    val commentTreeCache: StateFlow<Map<String, Comment>> = _commentTreeCache.asStateFlow()

    /**
     * Get comments for a specific post
     */
    fun getCommentsForPost(postId: String): StateFlow<CommentsState> {
        val currentCache = _commentsCache.value
        if (!currentCache.containsKey(postId)) {
            _commentsCache.value = currentCache + (postId to CommentsState.NotLoaded)
        }

        return MutableStateFlow(currentCache[postId] ?: CommentsState.NotLoaded).apply {
            // Update when cache changes
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                _commentsCache.collect { cache ->
                    cache[postId]?.let { value = it }
                }
            }
        }.asStateFlow()
    }

    /**
     * Load comments for a post from Reddit API
     */
    suspend fun loadComments(
        subreddit: String,
        postId: String,
        sort: CommentSort = CommentSort.CONFIDENCE,
        forceRefresh: Boolean = false
    ): Result<List<Comment>> {
        // Check cache first
        val currentState = _commentsCache.value[postId]
        if (!forceRefresh && currentState is CommentsState.Loaded) {
            Logger.d( "Returning cached comments for post: $postId")
            return Result.success(currentState.comments)
        }

        // Set loading state
        updateCommentsState(postId, CommentsState.Loading)

        return try {
            val response = if (profileManager.isLoggedIn()) {
                val apiService = profileManager.getAuthenticatedApiService()
                    ?: throw IllegalStateException("User not logged in")
                apiService.getPostComments(subreddit, postId, sort.value, limit = 200)
            } else {
                val apiService = com.hamburghini.cosmos.core.network.RetrofitClient.publicRedditApiService
                apiService.getPostComments(subreddit, postId, sort.value, limit = 200)
            }

            if (response.isSuccessful) {
                val listings = response.body()
                if (listings != null && listings.size >= 2) {
                    // Second listing contains comments
                    val commentsListing = listings[1]
                    val comments = parseComments(commentsListing)

                    // Update cache
                    updateCommentsState(postId, CommentsState.Loaded(comments, sort))

                    // Cache individual comments for tree navigation
                    cacheCommentTree(comments)

                    Logger.d( "Loaded ${comments.size} top-level comments for post: $postId")
                    Result.success(comments)
                } else {
                    val error = "Invalid response structure"
                    updateCommentsState(postId, CommentsState.Error(error))
                    Result.failure(Exception(error))
                }
            } else {
                val error = "Failed to load comments: ${response.code()}"
                updateCommentsState(postId, CommentsState.Error(error))
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Logger.e( "Error loading comments for post: $postId", e)
            val error = "Error loading comments: ${e.message}"
            updateCommentsState(postId, CommentsState.Error(error))
            Result.failure(e)
        }
    }

    /**
     * Load more children comments (when "load more" is clicked)
     */
    suspend fun loadMoreChildren(
        linkId: String,
        children: List<String>,
        sort: CommentSort = CommentSort.CONFIDENCE
    ): Result<List<Comment>> {
        return try {
            val childrenStr = children.joinToString(",")

            val response = if (profileManager.isLoggedIn()) {
                val apiService = profileManager.getAuthenticatedApiService()
                    ?: throw IllegalStateException("User not logged in")
                apiService.getMoreComments(linkId, childrenStr, sort.value)
            } else {
                val apiService = com.hamburghini.cosmos.core.network.RetrofitClient.publicRedditApiService
                apiService.getMoreComments(linkId, childrenStr, sort.value)
            }

            if (response.isSuccessful) {
                val moreResponse = response.body()
                if (moreResponse?.json?.data?.things != null) {
                    val newComments = moreResponse.json.data.things.map { it.data }
                    cacheCommentTree(newComments)
                    Logger.d( "Loaded ${newComments.size} more children comments")
                    Result.success(newComments)
                } else {
                    Result.failure(Exception("No data in response"))
                }
            } else {
                Result.failure(Exception("Failed to load more comments: ${response.code()}"))
            }
        } catch (e: Exception) {
            Logger.e( "Error loading more children", e)
            Result.failure(e)
        }
    }

    /**
     * Vote on a comment
     */
    suspend fun voteOnComment(
        commentId: String,
        direction: Int
    ): Result<Unit> {
        return try {
            val apiService = profileManager.getAuthenticatedApiService()
                ?: return Result.failure(IllegalStateException("User not logged in"))

            val response = apiService.vote(commentId, direction)

            if (response.isSuccessful) {
                // Update local cache with new vote state
                updateCommentVote(commentId, direction)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Vote failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Logger.e( "Error voting on comment: $commentId", e)
            Result.failure(e)
        }
    }

    /**
     * Submit a new comment or reply
     */
    suspend fun submitComment(
        parentId: String,
        text: String
    ): Result<Comment> {
        return try {
            val apiService = profileManager.getAuthenticatedApiService()
                ?: return Result.failure(IllegalStateException("User not logged in"))

            val response = apiService.submitComment(parentId, text)

            if (response.isSuccessful) {
                val commentResponse = response.body()
                if (commentResponse?.json?.data?.things?.isNotEmpty() == true) {
                    val newComment = commentResponse.json.data.things[0].data

                    // Add to cache
                    cacheCommentTree(listOf(newComment))

                    // Update parent post's comments if cached
                    addNewCommentToCache(parentId, newComment)

                    Result.success(newComment)
                } else {
                    Result.failure(Exception("No comment data in response"))
                }
            } else {
                Result.failure(Exception("Submit failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Logger.e( "Error submitting comment", e)
            Result.failure(e)
        }
    }

    /**
     * Edit a comment
     */
    suspend fun editComment(
        commentId: String,
        newText: String
    ): Result<Unit> {
        return try {
            val apiService = profileManager.getAuthenticatedApiService()
                ?: return Result.failure(IllegalStateException("User not logged in"))

            val response = apiService.editText(commentId, newText)

            if (response.isSuccessful) {
                // Update local cache
                updateCommentText(commentId, newText)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Edit failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Logger.e( "Error editing comment: $commentId", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a comment
     */
    suspend fun deleteComment(commentId: String): Result<Unit> {
        return try {
            val apiService = profileManager.getAuthenticatedApiService()
                ?: return Result.failure(IllegalStateException("User not logged in"))

            val response = apiService.delete(commentId)

            if (response.isSuccessful) {
                // Mark as deleted in cache
                markCommentAsDeleted(commentId)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Logger.e( "Error deleting comment: $commentId", e)
            Result.failure(e)
        }
    }

    /**
     * Clear comments cache for a specific post
     */
    fun clearCommentsForPost(postId: String) {
        val currentCache = _commentsCache.value.toMutableMap()
        currentCache.remove(postId)
        _commentsCache.value = currentCache
        Logger.d( "Cleared comments cache for post: $postId")
    }

    /**
     * Clear all comments cache
     */
    fun clearAllComments() {
        _commentsCache.value = emptyMap()
        _commentTreeCache.value = emptyMap()
        Logger.d( "Cleared all comments cache")
    }

    // Private helper methods

    private fun updateCommentsState(postId: String, state: CommentsState) {
        val currentCache = _commentsCache.value.toMutableMap()
        currentCache[postId] = state
        _commentsCache.value = currentCache
    }

    private fun parseComments(listing: RedditListingResponse<Comment>): List<Comment> {
        return listing.data.children
            .filter { it.kind == "t1" } // Only comments, not "more" objects
            .map { it.data }
    }

    private fun cacheCommentTree(comments: List<Comment>) {
        val currentTree = _commentTreeCache.value.toMutableMap()
        comments.forEach { comment ->
            currentTree[comment.name] = comment
        }
        _commentTreeCache.value = currentTree
    }

    private fun updateCommentVote(commentId: String, direction: Int) {
        // Update in all relevant caches
        _commentsCache.value = _commentsCache.value.mapValues { (_, state) ->
            if (state is CommentsState.Loaded) {
                CommentsState.Loaded(
                    comments = updateCommentInList(state.comments, commentId, direction),
                    sort = state.sort
                )
            } else {
                state
            }
        }

        // Update in tree cache
        val tree = _commentTreeCache.value.toMutableMap()
        tree[commentId]?.let { comment ->
            val newLikes = when (direction) {
                1 -> true
                -1 -> false
                else -> null
            }
            tree[commentId] = comment.copy(
                score = comment.score + when {
                    comment.score == 0 && direction == 1 -> 1
                    comment.score == 0 && direction == -1 -> -1
                    else -> 0
                }
            )
        }
        _commentTreeCache.value = tree
    }

    private fun updateCommentInList(
        comments: List<Comment>,
        commentId: String,
        direction: Int
    ): List<Comment> {
        return comments.map { comment ->
            if (comment.name == commentId) {
                comment.copy(
                    score = comment.score + when (direction) {
                        1 -> 1
                        -1 -> -1
                        else -> 0
                    }
                )
            } else {
                comment
            }
        }
    }

    private fun updateCommentText(commentId: String, newText: String) {
        // Update in tree cache
        val tree = _commentTreeCache.value.toMutableMap()
        tree[commentId]?.let { comment ->
            tree[commentId] = comment.copy(body = newText)
        }
        _commentTreeCache.value = tree

        // Update in all post caches
        _commentsCache.value = _commentsCache.value.mapValues { (_, state) ->
            if (state is CommentsState.Loaded) {
                CommentsState.Loaded(
                    comments = updateTextInList(state.comments, commentId, newText),
                    sort = state.sort
                )
            } else {
                state
            }
        }
    }

    private fun updateTextInList(
        comments: List<Comment>,
        commentId: String,
        newText: String
    ): List<Comment> {
        return comments.map { comment ->
            if (comment.name == commentId) {
                comment.copy(body = newText)
            } else {
                comment
            }
        }
    }

    private fun markCommentAsDeleted(commentId: String) {
        // Update in tree cache
        val tree = _commentTreeCache.value.toMutableMap()
        tree[commentId]?.let { comment ->
            tree[commentId] = comment.copy(
                body = "[deleted]",
                author = "[deleted]"
            )
        }
        _commentTreeCache.value = tree

        // Update in all post caches
        _commentsCache.value = _commentsCache.value.mapValues { (_, state) ->
            if (state is CommentsState.Loaded) {
                CommentsState.Loaded(
                    comments = markDeletedInList(state.comments, commentId),
                    sort = state.sort
                )
            } else {
                state
            }
        }
    }

    private fun markDeletedInList(
        comments: List<Comment>,
        commentId: String
    ): List<Comment> {
        return comments.map { comment ->
            if (comment.name == commentId) {
                comment.copy(
                    body = "[deleted]",
                    author = "[deleted]"
                )
            } else {
                comment
            }
        }
    }

    private fun addNewCommentToCache(parentId: String, newComment: Comment) {
        // Find which post this belongs to by checking if parentId is in any cached post
        _commentsCache.value = _commentsCache.value.mapValues { (postId, state) ->
            if (state is CommentsState.Loaded) {
                // Check if this post contains the parent
                val hasParent = state.comments.any { it.name == parentId }
                if (hasParent) {
                    CommentsState.Loaded(
                        comments = listOf(newComment) + state.comments,
                        sort = state.sort
                    )
                } else {
                    state
                }
            } else {
                state
            }
        }
    }
}

/**
 * Sealed class representing different states of comments for a post
 */
sealed class CommentsState {
    data object NotLoaded : CommentsState()
    data object Loading : CommentsState()
    data class Loaded(
        val comments: List<Comment>,
        val sort: CommentSort
    ) : CommentsState()
    data class Error(val message: String) : CommentsState()
}

/**
 * Comment sort options
 */
enum class CommentSort(val value: String) {
    CONFIDENCE("confidence"),
    TOP("top"),
    NEW("new"),
    CONTROVERSIAL("controversial"),
    OLD("old"),
    QA("qa")
}