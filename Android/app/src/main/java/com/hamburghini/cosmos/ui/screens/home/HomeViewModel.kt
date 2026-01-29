package com.hamburghini.cosmos.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamburghini.cosmos.core.util.Logger
import com.hamburghini.cosmos.data.manager.ProfileManager
import com.hamburghini.cosmos.data.model.AuthState
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.data.repository.ActionResult
import com.hamburghini.cosmos.data.repository.LoadType
import com.hamburghini.cosmos.data.repository.PostsState
import com.hamburghini.cosmos.data.repository.RedditRepository
import com.hamburghini.cosmos.data.repository.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RedditRepository,
    private val profileManager: ProfileManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    val postsState = repository.postsState

    private var currentAfter: String? = null
    private var lastAuthStateWasLoggedIn: Boolean? = null

    init {
        // Observe authentication state and load appropriate feed
        viewModelScope.launch {
            profileManager.authState.collect { authState ->
                handleAuthStateChange(authState)
            }
        }
    }

    /**
     * Handle authentication state changes
     * Load posts immediately on first state emission
     */
    private fun handleAuthStateChange(authState: AuthState) {
        when (authState) {
            is AuthState.LoggedIn -> {
                val wasLoggedIn = lastAuthStateWasLoggedIn
                lastAuthStateWasLoggedIn = true

                // If first time or user just logged in, load personalized feed
                if (wasLoggedIn == null || !wasLoggedIn) {
                    loadPosts(LoadType.INITIAL, _uiState.value.currentSortType)
                }
            }
            is AuthState.NotLoggedIn -> {
                val wasLoggedIn = lastAuthStateWasLoggedIn
                lastAuthStateWasLoggedIn = false

                // If first time or user just logged out, load public feed
                if (wasLoggedIn == null || wasLoggedIn == true) {
                    loadPosts(LoadType.INITIAL, _uiState.value.currentSortType)
                }
            }
            is AuthState.LoggingIn -> {
                // Don't reload while logging in - wait for final state
                // If this is first state and we have no posts, show loading
//                if (lastAuthStateWasLoggedIn == null && _posts.value.isEmpty()) {
//                    _uiState.value = _uiState.value.copy(isLoading = true)
//                }
            }
            is AuthState.AuthError -> {
                val wasLoggedIn = lastAuthStateWasLoggedIn
                lastAuthStateWasLoggedIn = false

                // Load public feed on error
                if (wasLoggedIn == null || wasLoggedIn == true) {
                    loadPosts(LoadType.INITIAL, _uiState.value.currentSortType)
                }
            }
        }
    }

    fun loadPosts(
        loadType: LoadType,
        sortType: SortType = SortType.HOT
    ) {
        // Don't reload if we're already loading the same sort type, unless forced
//        if (!forceRefresh && _uiState.value.currentSortType == sortType) return

        viewModelScope.launch {
            if (loadType == LoadType.MORE || loadType == LoadType.INITIAL) {
                _uiState.value = _uiState.value.copy(isLoadingMore = true)
            }

            try {
                val isLoggedIn = profileManager.isLoggedIn()
                if (isLoggedIn) {
                    // Load user's personalized feed
                    loadAuthenticatedPosts(sortType, loadType)
                } else {
                    // Load public feed
                    loadPublicPosts(sortType, loadType)
                }
            } catch (e: IllegalStateException) {
                // User not logged in for authenticated endpoint
                if (e.message?.contains("not logged in") == true) {
                    // Fallback to public feed
                    loadPublicPosts(sortType, loadType)
                } else {
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        error = "Error: ${e.message}"
//                    )
                }
            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    error = "Error loading posts: ${e.message}"
//                )
            }
        }
    }

    private fun loadAuthenticatedPosts(sortType: SortType, loadType: LoadType) {
        viewModelScope.launch {
            when (sortType) {
                SortType.HOT -> repository.getMyHotPosts(loadType)
                SortType.BEST -> repository.getMyBestPosts(loadType)
                SortType.NEW -> repository.getMyNewPosts(loadType)
                SortType.TOP -> repository.getMyTopPosts(loadType = loadType, timeframe = "day")
                SortType.RISING -> repository.getHotPosts(loadType = loadType, subreddit = "all") // Fallback to public for rising
            }
        }
    }

    private fun loadPublicPosts(sortType: SortType, loadType: LoadType) {
        viewModelScope.launch {
            when (sortType) {
                SortType.HOT -> repository.getHotPosts(loadType, "all")
                SortType.BEST -> repository.getHotPosts(loadType, "all") // Fallback to hot for public
                SortType.NEW -> repository.getNewPosts(loadType, "all")
                SortType.TOP -> repository.getTopPosts(loadType, "all", "day")
                SortType.RISING -> repository.getRisingPosts(loadType, "all")
            }
        }
    }

    fun clearError() {
//        _uiState.value = _uiState.value.copy(error = null)
    }

    fun voteOnPost(postId: String, direction: Int) {
        if (!profileManager.isLoggedIn()) {
            viewModelScope.launch {
                _events.emit(
                    UiEvent.ShowMessage("Please log in to vote")
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                when (val response = repository.vote(postId, direction)) {
                    is ActionResult.Failure -> {
                        _events.emit(
                            UiEvent.ShowMessage(response.reason)
                        )
                    }
                    ActionResult.Success -> Unit
                }
            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    error = "Error voting: ${e.message}"
//                )
            }
        }
    }

    fun savePost(postId: String, save: Boolean) {
        if (!profileManager.isLoggedIn()) {
            viewModelScope.launch {
                _events.emit(
                    UiEvent.ShowMessage("Please log in to save posts")
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                val response = if (save) {
                    repository.savePost(postId)
                } else {
                    repository.unsavePost(postId)
                }

                when (response) {
                    is ActionResult.Failure -> {
                        _events.emit(
                            UiEvent.ShowMessage(response.reason)
                        )
                    }
                    ActionResult.Success -> Unit
                }
            } catch (e: Exception) {
                val action = if (save) "save" else "unsave"
//                _uiState.value = _uiState.value.copy(
//                    error = "Error ${action}ing post: ${e.message}"
//                )
            }
        }
    }

    /**
     * Get a specific post by ID from the current list
     */
    fun getPost(postId: String): Post? {
        if (postsState.value is PostsState.Success) {
            val map = (postsState.value as PostsState.Success).posts
            return map[postId]
        }
        return null
    }

    fun getCurrentUsername(): String {
        return profileManager.getDisplayUsername()
    }

    fun isLoggedIn(): Boolean {
        return profileManager.isLoggedIn()
    }

    /**
     * Force load posts for a specific sort type (called from UI)
     */
    fun loadPostsForSort(sortType: SortType) {
        loadPosts(LoadType.INITIAL, sortType)
    }
}

data class HomeUiState(
    val isLoadingMore: Boolean = false,
    val currentSortType: SortType = SortType.HOT,
    val isPersonalized: Boolean = false
)

sealed interface UiEvent {
    data class ShowMessage(val message: String) : UiEvent
}