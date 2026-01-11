package com.hamburghini.cosmos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repository: RedditRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private var currentAfter: String? = null

    init {
        loadPosts()
    }

    fun loadPosts(sortType: SortType = SortType.HOT) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val response = when (sortType) {
                    SortType.HOT -> repository.getHotPosts("all")
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
                            hasMore = listing.data.after != null
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
                val response = when (_uiState.value.currentSortType) {
                    SortType.HOT -> repository.getHotPosts("all", currentAfter)
                    SortType.NEW -> repository.getNewPosts("all", currentAfter)
                    SortType.TOP -> repository.getTopPosts("all", "day", currentAfter)
                    SortType.RISING -> repository.getRisingPosts("all", currentAfter)
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
        loadPosts(_uiState.value.currentSortType)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentSortType: SortType = SortType.HOT,
    val hasMore: Boolean = false
)

enum class SortType {
    HOT, NEW, TOP, RISING
}