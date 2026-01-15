package com.hamburghini.cosmos.ui.screens.postdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.hamburghini.cosmos.data.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
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