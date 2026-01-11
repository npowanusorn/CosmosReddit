package com.hamburghini.cosmos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamburghini.cosmos.manager.ProfileManager
import com.hamburghini.cosmos.model.AuthState
import com.hamburghini.cosmos.model.SubredditAboutData
import com.hamburghini.cosmos.repository.RedditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubredditListViewModel @Inject constructor(
    private val repository: RedditRepository,
    private val profileManager: ProfileManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubredditListUiState())
    val uiState: StateFlow<SubredditListUiState> = _uiState.asStateFlow()

    private val _mySubreddits = MutableStateFlow<List<SubredditAboutData>>(emptyList())
    val mySubreddits: StateFlow<List<SubredditAboutData>> = _mySubreddits.asStateFlow()

    private val _popularSubreddits = MutableStateFlow<List<SubredditAboutData>>(emptyList())
    val popularSubreddits: StateFlow<List<SubredditAboutData>> = _popularSubreddits.asStateFlow()

    val authState: StateFlow<AuthState> = profileManager.authState

    private var mySubredditsAfter: String? = null
    private var popularSubredditsAfter: String? = null

    init {
        // Load subreddits based on auth state
        viewModelScope.launch {
            profileManager.authState.collect { authState ->
                when (authState) {
                    is AuthState.LoggedIn -> {
                        // User is logged in - load both my subreddits and popular
                        loadMySubreddits()
                        loadPopularSubreddits()
                    }
                    is AuthState.NotLoggedIn -> {
                        // User is not logged in - only load popular
                        loadPopularSubreddits()
                    }
                    else -> {
                        // Do nothing for LoggingIn or AuthError states
                    }
                }
            }
        }
    }

    /**
     * Load user's subscribed subreddits (requires authentication)
     */
    fun loadMySubreddits(forceRefresh: Boolean = false) {
        if (!forceRefresh && (_uiState.value.isLoadingMy || _mySubreddits.value.isNotEmpty())) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMy = true, errorMy = null)
            mySubredditsAfter = null

            try {
                val response = repository.getMySubscribedSubreddits()

                if (response.isSuccessful) {
                    val listing = response.body()
                    if (listing != null) {
                        val subreddits = listing.data.children.map { it.data }
                        _mySubreddits.value = subreddits
                        mySubredditsAfter = listing.data.after
                        _uiState.value = _uiState.value.copy(
                            isLoadingMy = false,
                            hasMoreMy = listing.data.after != null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoadingMy = false,
                            errorMy = "No data received"
                        )
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> {
                            // Try to refresh token
                            if (profileManager.getCurrentAccount()?.refreshToken?.isNotBlank() == true) {
                                val refreshResult = profileManager.refreshCurrentToken()
                                if (refreshResult) {
                                    // Retry loading
                                    loadMySubreddits(forceRefresh = true)
                                    return@launch
                                }
                            }
                            "Session expired - please log in again"
                        }
                        403 -> "Access denied"
                        429 -> "Too many requests - please wait"
                        500, 502, 503 -> "Reddit server error - try again later"
                        else -> "Failed to load subscriptions (${response.code()})"
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoadingMy = false,
                        errorMy = errorMessage
                    )
                }
            } catch (e: IllegalStateException) {
                // User not logged in
                _uiState.value = _uiState.value.copy(
                    isLoadingMy = false,
                    errorMy = "Please log in to view subscriptions"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMy = false,
                    errorMy = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Load more of user's subscribed subreddits
     */
    fun loadMoreMySubreddits() {
        if (_uiState.value.isLoadingMoreMy || mySubredditsAfter == null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMoreMy = true)

            try {
                val response = repository.getMySubscribedSubreddits(mySubredditsAfter)

                if (response.isSuccessful) {
                    val listing = response.body()
                    if (listing != null) {
                        val newSubreddits = listing.data.children.map { it.data }
                        _mySubreddits.value += newSubreddits
                        mySubredditsAfter = listing.data.after
                        _uiState.value = _uiState.value.copy(
                            isLoadingMoreMy = false,
                            hasMoreMy = listing.data.after != null
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMoreMy = false,
                        errorMy = "Failed to load more: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMoreMy = false,
                    errorMy = "Error loading more: ${e.message}"
                )
            }
        }
    }

    /**
     * Load popular subreddits (no authentication required)
     */
    fun loadPopularSubreddits(forceRefresh: Boolean = false) {
        if (!forceRefresh && (_uiState.value.isLoadingPopular || _popularSubreddits.value.isNotEmpty())) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPopular = true, errorPopular = null)
            popularSubredditsAfter = null

            try {
                val response = repository.getPopularSubreddits()

                if (response.isSuccessful) {
                    val listing = response.body()
                    if (listing != null) {
                        val subreddits = listing.data.children.map { it.data }
                        _popularSubreddits.value = subreddits
                        popularSubredditsAfter = listing.data.after
                        _uiState.value = _uiState.value.copy(
                            isLoadingPopular = false,
                            hasMorePopular = listing.data.after != null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoadingPopular = false,
                            errorPopular = "No data received"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingPopular = false,
                        errorPopular = "Failed to load popular subreddits: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingPopular = false,
                    errorPopular = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Load more popular subreddits
     */
    fun loadMorePopularSubreddits() {
        if (_uiState.value.isLoadingMorePopular || popularSubredditsAfter == null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMorePopular = true)

            try {
                val response = repository.getPopularSubreddits(popularSubredditsAfter)

                if (response.isSuccessful) {
                    val listing = response.body()
                    if (listing != null) {
                        val newSubreddits = listing.data.children.map { it.data }
                        _popularSubreddits.value += newSubreddits
                        popularSubredditsAfter = listing.data.after
                        _uiState.value = _uiState.value.copy(
                            isLoadingMorePopular = false,
                            hasMorePopular = listing.data.after != null
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMorePopular = false,
                        errorPopular = "Failed to load more: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMorePopular = false,
                    errorPopular = "Error loading more: ${e.message}"
                )
            }
        }
    }

    /**
     * Subscribe to a subreddit
     */
    fun subscribeToSubreddit(subredditName: String, isSubscribe: Boolean) {
        if (!profileManager.isLoggedIn()) {
            _uiState.value = _uiState.value.copy(errorPopular = "Please log in to subscribe")
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.subscribeToSubreddit(
                    subredditName,
                    if (isSubscribe) "sub" else "unsub"
                )

                if (response.isSuccessful) {
                    // Update local state
                    if (isSubscribe) {
                        // Find subreddit in popular list and add to my subreddits
                        val subreddit = _popularSubreddits.value.find { it.name == subredditName }
                        if (subreddit != null) {
                            _mySubreddits.value = listOf(subreddit.copy(userIsSubscriber = true)) + _mySubreddits.value
                        }
                    } else {
                        // Remove from my subreddits
                        _mySubreddits.value = _mySubreddits.value.filter { it.name != subredditName }
                    }

                    // Update userIsSubscriber in popular list
                    _popularSubreddits.value = _popularSubreddits.value.map { subreddit ->
                        if (subreddit.name == subredditName) {
                            subreddit.copy(userIsSubscriber = isSubscribe)
                        } else {
                            subreddit
                        }
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorPopular = "Failed to ${if (isSubscribe) "subscribe" else "unsubscribe"}: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorPopular = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Refresh all subreddit lists
     */
    fun refreshAll() {
        if (profileManager.isLoggedIn()) {
            loadMySubreddits(forceRefresh = true)
        }
        loadPopularSubreddits(forceRefresh = true)
    }

    /**
     * Clear errors
     */
    fun clearErrors() {
        _uiState.value = _uiState.value.copy(errorMy = null, errorPopular = null)
    }

    /**
     * Check if user is subscribed to a subreddit
     */
    fun isSubscribed(subredditName: String): Boolean {
        return _mySubreddits.value.any { it.name == subredditName }
    }
}

data class SubredditListUiState(
    val isLoadingMy: Boolean = false,
    val isLoadingMoreMy: Boolean = false,
    val isLoadingPopular: Boolean = false,
    val isLoadingMorePopular: Boolean = false,
    val errorMy: String? = null,
    val errorPopular: String? = null,
    val hasMoreMy: Boolean = false,
    val hasMorePopular: Boolean = false
)