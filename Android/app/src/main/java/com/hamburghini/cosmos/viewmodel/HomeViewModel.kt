package com.hamburghini.cosmos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamburghini.cosmos.manager.ProfileManager
import com.hamburghini.cosmos.model.AuthState
import com.hamburghini.cosmos.model.Post
import com.hamburghini.cosmos.repository.RedditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RedditRepository,
    private val profileManager: ProfileManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private var currentAfter: String? = null
    private var hasInitializedWithAuthState = false

    init {
        // Observe authentication state changes and load appropriate feed
        viewModelScope.launch {
            profileManager.authState.collect { authState ->
                when (authState) {
                    is AuthState.LoggedIn -> {
                        // User is logged in - load personalized feed
                        if (!hasInitializedWithAuthState) {
                            // First time initialization - load personalized feed
                            hasInitializedWithAuthState = true
                            loadPosts(_uiState.value.currentSortType)
                        } else {
                            // User just logged in - switch to personalized feed
                            loadPosts(_uiState.value.currentSortType, forceRefresh = true)
                        }
                    }
                    is AuthState.NotLoggedIn -> {
                        if (!hasInitializedWithAuthState) {
                            // First time initialization - load public feed
                            hasInitializedWithAuthState = true
                            loadPosts(_uiState.value.currentSortType)
                        } else {
                            // User just logged out - switch to public feed
                            loadPosts(_uiState.value.currentSortType, forceRefresh = true)
                        }
                    }
                    is AuthState.LoggingIn -> {
                        // Don't load posts while logging in, wait for final state
                        if (!hasInitializedWithAuthState) {
                            // If we're starting in LoggingIn state, wait for completion
                            // This can happen if user was previously logged in and app is restoring session
                        }
                    }
                    is AuthState.AuthError -> {
                        if (!hasInitializedWithAuthState) {
                            // Auth failed on startup - load public feed
                            hasInitializedWithAuthState = true
                            loadPosts(_uiState.value.currentSortType)
                        }
                        // If auth fails after being logged in, we'll already have posts loaded
                    }
                }
            }
        }
    }

    fun loadPosts(sortType: SortType = SortType.HOT, forceRefresh: Boolean = false) {
        // Don't reload if we're already loading the same sort type, unless forced
        if (!forceRefresh &&
            _uiState.value.isLoading &&
            _uiState.value.currentSortType == sortType) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            currentAfter = null

            try {
                val isLoggedIn = profileManager.isLoggedIn()
                val response = if (isLoggedIn) {
                    // Load user's personalized feed
                    when (sortType) {
                        SortType.HOT -> repository.getMyHotPosts()
                        SortType.BEST -> repository.getMyBestPosts()
                        SortType.NEW -> repository.getMyNewPosts()
                        SortType.TOP -> repository.getMyTopPosts(timeframe = "day")
                        SortType.RISING -> repository.getHotPosts("all") // Fallback to public for rising
                    }
                } else {
                    // Load public feed
                    when (sortType) {
                        SortType.HOT -> repository.getHotPosts("all")
                        SortType.BEST -> repository.getHotPosts("all") // Fallback to hot for public
                        SortType.NEW -> repository.getNewPosts("all")
                        SortType.TOP -> repository.getTopPosts("all", "day")
                        SortType.RISING -> repository.getRisingPosts("all")
                    }
                }

                if (response.isSuccessful) {
                    val listing = response.body()
                    if (listing != null) {
                        val newPosts = listing.data.children.map { it.data }
                        _posts.value = newPosts
                        currentAfter = listing.data.after
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentSortType = sortType,
                            hasMore = listing.data.after != null,
                            isPersonalized = isLoggedIn && sortType != SortType.RISING
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "No data received"
                        )
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> {
                            // Token might be expired, try to refresh
                            if (isLoggedIn && profileManager.getCurrentAccount()?.refreshToken?.isNotBlank() == true) {
                                // Try to refresh token and reload
                                val refreshResult = profileManager.refreshCurrentToken()
                                if (refreshResult) {
                                    // Token refreshed, retry loading posts
                                    loadPosts(sortType, forceRefresh = true)
                                    return@launch
                                }
                            }
                            "Session expired - please log in again"
                        }
                        403 -> "Access denied - check permissions"
                        429 -> "Too many requests - please wait"
                        500, 502, 503 -> "Reddit server error - try again later"
                        else -> "Failed to load posts (${response.code()})"
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            } catch (e: IllegalStateException) {
                // User not logged in for authenticated endpoint
                if (e.message?.contains("not logged in") == true) {
                    // Fallback to public feed
                    loadPublicPosts(sortType)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error loading posts: ${e.message}"
                )
            }
        }
    }

    private fun loadPublicPosts(sortType: SortType) {
        viewModelScope.launch {
            try {
                val response = when (sortType) {
                    SortType.HOT, SortType.BEST -> repository.getHotPosts("all")
                    SortType.NEW -> repository.getNewPosts("all")
                    SortType.TOP -> repository.getTopPosts("all", "day")
                    SortType.RISING -> repository.getRisingPosts("all")
                }

                if (response.isSuccessful) {
                    val listing = response.body()
                    if (listing != null) {
                        val newPosts = listing.data.children.map { it.data }
                        _posts.value = newPosts
                        currentAfter = listing.data.after
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentSortType = sortType,
                            hasMore = listing.data.after != null,
                            isPersonalized = false
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load posts: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error loading posts: ${e.message}"
                )
            }
        }
    }

    fun loadMorePosts() {
        if (_uiState.value.isLoadingMore || currentAfter == null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)

            try {
                val isLoggedIn = profileManager.isLoggedIn()
                val response = if (isLoggedIn && _uiState.value.isPersonalized) {
                    // Load more from user's personalized feed
                    when (_uiState.value.currentSortType) {
                        SortType.HOT -> repository.getMyHotPosts(currentAfter)
                        SortType.BEST -> repository.getMyBestPosts(currentAfter)
                        SortType.NEW -> repository.getMyNewPosts(currentAfter)
                        SortType.TOP -> repository.getMyTopPosts(currentAfter, timeframe = "day")
                        SortType.RISING -> repository.getHotPosts("all", currentAfter)
                    }
                } else {
                    // Load more from public feed
                    when (_uiState.value.currentSortType) {
                        SortType.HOT, SortType.BEST -> repository.getHotPosts("all", currentAfter)
                        SortType.NEW -> repository.getNewPosts("all", currentAfter)
                        SortType.TOP -> repository.getTopPosts("all", "day", currentAfter)
                        SortType.RISING -> repository.getRisingPosts("all", currentAfter)
                    }
                }

                if (response.isSuccessful) {
                    val listing = response.body()
                    if (listing != null) {
                        val newPosts = listing.data.children.map { it.data }
                        _posts.value += newPosts
                        currentAfter = listing.data.after
                        _uiState.value = _uiState.value.copy(
                            isLoadingMore = false,
                            hasMore = listing.data.after != null
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        error = "Failed to load more posts: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    error = "Error loading more posts: ${e.message}"
                )
            }
        }
    }

    fun refreshPosts() {
        currentAfter = null
        loadPosts(_uiState.value.currentSortType, forceRefresh = true)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun voteOnPost(postId: String, direction: Int) {
        if (!profileManager.isLoggedIn()) {
            _uiState.value = _uiState.value.copy(error = "Please log in to vote")
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.vote(postId, direction)
                if (!response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to vote: ${response.code()}"
                    )
                }
                // If successful, the post score will be updated in the UI layer
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error voting: ${e.message}"
                )
            }
        }
    }

    fun savePost(postId: String, save: Boolean) {
        if (!profileManager.isLoggedIn()) {
            _uiState.value = _uiState.value.copy(error = "Please log in to save posts")
            return
        }

        viewModelScope.launch {
            try {
                val response = if (save) {
                    repository.savePost(postId)
                } else {
                    repository.unsavePost(postId)
                }

                if (response.isSuccessful) {
                    // Update the local post state immediately
                    updatePostSavedState(postId, save)
                } else {
                    val action = if (save) "save" else "unsave"
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to $action post: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                val action = if (save) "save" else "unsave"
                _uiState.value = _uiState.value.copy(
                    error = "Error ${action}ing post: ${e.message}"
                )
            }
        }
    }

    /**
     * Update the saved state of a specific post in the local list
     */
    private fun updatePostSavedState(postId: String, saved: Boolean) {
        _posts.value = _posts.value.map { post ->
            if (post.name == postId) {
                post.copy(saved = saved)
            } else {
                post
            }
        }
    }

    /**
     * Get a specific post by ID from the current list
     */
    fun getPost(postId: String): Post? {
        return _posts.value.find { it.name == postId }
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
        loadPosts(sortType, forceRefresh = false)
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentSortType: SortType = SortType.HOT,
    val hasMore: Boolean = false,
    val isPersonalized: Boolean = false // Whether showing user's personalized feed
)

enum class SortType {
    HOT, BEST, NEW, TOP, RISING
}