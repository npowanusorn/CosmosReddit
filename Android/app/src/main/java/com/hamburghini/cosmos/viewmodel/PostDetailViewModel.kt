package com.hamburghini.cosmos.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.hamburghini.cosmos.manager.ProfileManager
import com.hamburghini.cosmos.model.Post
import com.hamburghini.cosmos.repository.RedditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
//    private val repository: RedditRepository,
//    private val profileManager: ProfileManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val post: StateFlow<Post?> =
        savedStateHandle.getStateFlow("post", null)

    fun setPost(post: Post) {
        savedStateHandle["post"] = post
    }
}