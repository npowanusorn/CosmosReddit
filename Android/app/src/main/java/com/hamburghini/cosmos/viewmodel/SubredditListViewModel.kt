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
import java.util.Locale.getDefault
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

    private var popularSubredditsAfter: String? = null

    init {
        // Load subreddits based on auth state
        viewModelScope.launch {
            profileManager.authState.collect { authState ->
                when (authState) {
                    is AuthState.LoggedIn -> {
                        // User is logged in - load subscriptions
                        loadMySubscriptions()
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
     * Load user's subscriptions
     * Uses cache by default, pass forceRefresh=true to bypass cache
     */
    fun loadMySubscriptions(forceRefresh: Boolean = false) {
        if (!forceRefresh && (_uiState.value.isLoadingMy || _mySubreddits.value.isNotEmpty())) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingMy = true,
                errorMy = null,
                loadingProgress = if (forceRefresh) "Refreshing subscriptions..." else null
            )

            try {
                // useCache=true by default, set to false for force refresh
                val response = repository.getMySubscribedSubreddits(
                    after = null,
                    limit = 100,
                    useCache = !forceRefresh,
                    saveToCache = true
                )

                if (response.isSuccessful) {
                    val listing = response.body()
                    if (listing != null) {
                        val subreddits = listing.data.children.map { it.data }.sortedBy { it.displayName.lowercase(getDefault()) }
                        _mySubreddits.value = subreddits

                        _uiState.value = _uiState.value.copy(
                            isLoadingMy = false,
                            hasMoreMy = false, // All subscriptions loaded at once
                            loadingProgress = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoadingMy = false,
                            errorMy = "No data received",
                            loadingProgress = null
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
                                    loadMySubscriptions(forceRefresh = true)
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
                        errorMy = errorMessage,
                        loadingProgress = null
                    )
                }
            } catch (e: IllegalStateException) {
                // User not logged in
                _uiState.value = _uiState.value.copy(
                    isLoadingMy = false,
                    errorMy = "Please log in to view subscriptions",
                    loadingProgress = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMy = false,
                    errorMy = "Error: ${e.message}",
                    loadingProgress = null
                )
            }
        }
    }

    /**
     * Force refresh subscriptions from API (called on pull-to-refresh)
     */
    fun refreshMySubscriptions() {
        loadMySubscriptions(forceRefresh = true)
    }

    /**
     * Subscribe/unsubscribe to a subreddit
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
                    // Update local state immediately for better UX
                    if (isSubscribe) {
                        val subreddit = _popularSubreddits.value.find { it.name == subredditName }
                        if (subreddit != null) {
                            val newList = listOf(subreddit.copy(userIsSubscriber = true)) + _mySubreddits.value
                            _mySubreddits.value = newList.sortedBy { it.displayName.lowercase(getDefault()) }
                        }
                    } else {
                        _mySubreddits.value = _mySubreddits.value.filter { it.name != subredditName }.sortedBy {
                            it.displayName.lowercase(getDefault())
                        }
                    }

                    _popularSubreddits.value = _popularSubreddits.value.map { subreddit ->
                        if (subreddit.name == subredditName) {
                            subreddit.copy(userIsSubscriber = isSubscribe)
                        } else {
                            subreddit
                        }
                    }

                    // Cache is automatically cleared by repository
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
     * Load more popular subreddits (pagination)
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
     * Refresh all subreddit lists (called on pull-to-refresh)
     */
    fun refreshAll() {
        if (profileManager.isLoggedIn()) {
            refreshMySubscriptions()
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
    val hasMorePopular: Boolean = false,
    val loadingProgress: String? = null
)