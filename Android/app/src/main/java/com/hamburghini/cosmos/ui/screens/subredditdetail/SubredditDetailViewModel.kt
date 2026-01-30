package com.hamburghini.cosmos.ui.screens.subredditdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamburghini.cosmos.core.util.Logger
import com.hamburghini.cosmos.data.manager.ProfileManager
import com.hamburghini.cosmos.data.repository.LoadType
import com.hamburghini.cosmos.data.repository.RedditRepository
import com.hamburghini.cosmos.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubredditDetailViewModel @Inject constructor(
    private val profileManager: ProfileManager,
    private val repository: RedditRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val subredditName = MutableStateFlow<String?>(null)

    val postsState = repository.postsState
    val blurNsfw = settingsRepository.blurNsfwFlow
        .onEach { isEnabled ->
            Logger.d("DataStore update: $isEnabled")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun setSubredditName(name: String) {
        subredditName.value = name

        viewModelScope.launch {
            repository.getHotPosts(LoadType.INITIAL, name)
        }
    }

    fun loadPosts(loadType: LoadType) {
        TODO("loadPosts")
    }
}

enum class SubredditDetailTabs(val title: String) {
    POST("Post"), ABOUT("About"), MENU("Menu")
}