package com.hamburghini.cosmos.ui.screens.subredditdetail

import androidx.lifecycle.ViewModel
import com.hamburghini.cosmos.data.manager.ProfileManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class SubredditDetailViewModel @Inject constructor(
    private val profileManager: ProfileManager
) : ViewModel() {

    private val subredditName = MutableStateFlow<String?>(null)

    fun setSubredditName(name: String) {
        subredditName.value = name
    }
}