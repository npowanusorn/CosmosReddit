package com.hamburghini.cosmos.ui.screens.postdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamburghini.cosmos.core.constants.Constants
import com.hamburghini.cosmos.data.manager.ProfileManager
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.data.repository.ActionResult
import com.hamburghini.cosmos.data.repository.CommentSort
import com.hamburghini.cosmos.data.repository.CommentsRepository
import com.hamburghini.cosmos.data.repository.CommentsState
import com.hamburghini.cosmos.data.repository.PostsState
import com.hamburghini.cosmos.data.repository.RedditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val commentsRepository: CommentsRepository,
    private val redditRepository: RedditRepository,
    private val savedStateHandle: SavedStateHandle,
    private val profileManager: ProfileManager
) : ViewModel() {

    private val postName = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val post: StateFlow<Post?> =
        postName
            .filterNotNull()
            .flatMapLatest { name ->
                redditRepository.postsState.map { state ->
                    (state as? PostsState.Success)?.posts?.get(name)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                null
            )


    private val _commentsState = MutableStateFlow<CommentsState>(CommentsState.NotLoaded)
    val commentsState: StateFlow<CommentsState> = _commentsState.asStateFlow()

    private val _currentSort = MutableStateFlow(CommentSort.CONFIDENCE)
    val currentSort: StateFlow<CommentSort> = _currentSort.asStateFlow()

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    fun setPostName(name: String) {
        postName.value = name
    }

    fun loadComments(sort: CommentSort = _currentSort.value, forceRefresh: Boolean = false) {
        val currentPost = post.value ?: return

        viewModelScope.launch {
            _commentsState.value = CommentsState.Loading

            val result = commentsRepository.loadComments(
                subreddit = currentPost.subreddit,
                postId = currentPost.id,
                sort = sort,
                forceRefresh = forceRefresh
            )

            result.onSuccess { comments ->
                _commentsState.value = CommentsState.Loaded(comments, sort)
                _currentSort.value = sort
            }.onFailure { error ->
                _commentsState.value = CommentsState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun changeSort(sort: CommentSort) {
        if (sort != _currentSort.value) {
            loadComments(sort, forceRefresh = true)
        }
    }

    fun refreshComments() {
        loadComments(forceRefresh = true)
    }

    fun voteOnComment(commentId: String, direction: Int) {
        viewModelScope.launch {
            val result = commentsRepository.voteOnComment(commentId, direction)
            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    error = "Failed to vote: ${error.message}"
                )
            }
        }
    }

    fun submitComment(parentId: String, text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingComment = true)

            val result = commentsRepository.submitComment(parentId, text)

            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSubmittingComment = false,
                    commentSubmitted = true
                )
                refreshComments()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSubmittingComment = false,
                    error = "Failed to post comment: ${error.message}"
                )
            }
        }
    }

    fun editComment(commentId: String, newText: String) {
        viewModelScope.launch {
            val result = commentsRepository.editComment(commentId, newText)
            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    error = "Failed to edit: ${error.message}"
                )
            }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            val result = commentsRepository.deleteComment(commentId)
            result.onSuccess {
                refreshComments()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete: ${error.message}"
                )
            }
        }
    }

    fun voteOnPost(direction: Int) {
        val currentPost = post.value ?: return

        viewModelScope.launch {
            val result = redditRepository.vote(currentPost.name, direction)
            if (result is ActionResult.Failure) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to vote: ${result.reason}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearCommentSubmittedFlag() {
        _uiState.value = _uiState.value.copy(commentSubmitted = false)
    }

    fun isLoggedIn(): Boolean {
        return profileManager.isLoggedIn()
    }

    fun savePost(name: String, saveState: Boolean) {

    }
}

data class PostDetailUiState(
    val isSubmittingComment: Boolean = false,
    val commentSubmitted: Boolean = false,
    val error: String? = null
)