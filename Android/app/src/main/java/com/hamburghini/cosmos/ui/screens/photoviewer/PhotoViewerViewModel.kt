package com.hamburghini.cosmos.ui.screens.photoviewer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamburghini.cosmos.data.manager.ProfileManager
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.data.repository.ActionResult
import com.hamburghini.cosmos.data.repository.PostsState
import com.hamburghini.cosmos.data.repository.RedditRepository
import com.hamburghini.cosmos.ui.screens.home.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PhotoViewerViewModel @Inject constructor(
    private val repository: RedditRepository,
    private val profileManager: ProfileManager
) : ViewModel() {

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    val postsState = repository.postsState

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

    fun postFlow(postId: String): StateFlow<Post?> {
        return postsState
            .map { state ->
                (state as? PostsState.Success)?.posts?.get(postId)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = getPost(postId)
            )
    }

    fun getPost(postId: String): Post? {
        if (postsState.value is PostsState.Success) {
            val map = (postsState.value as PostsState.Success).posts
            return map[postId]
        }
        return null
    }

    fun isLoggedIn(): Boolean {
        return profileManager.isLoggedIn()
    }

}