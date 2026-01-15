package com.hamburghini.cosmos.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamburghini.cosmos.core.constants.AppTheme
import com.hamburghini.cosmos.core.constants.AutoplayVideo
import com.hamburghini.cosmos.core.constants.PostSort
import com.hamburghini.cosmos.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {

    val theme = repo.themeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), AppTheme.SYSTEM
    )

    val compactMode = repo.compactModeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), false
    )

    val showNsfw = repo.showNsfwFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), false
    )

    val blurNsfw = repo.blurNsfwFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), true
    )

    val autoplayVideo = repo.autoplayVideoFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), AutoplayVideo.WIFI_ONLY
    )

    val defaultSort = repo.defaultSortFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), PostSort.HOT
    )

    fun setTheme(theme: AppTheme) = viewModelScope.launch {
        repo.setTheme(theme)
    }

    fun toggleCompactMode(value: Boolean) = viewModelScope.launch {
        repo.setCompactMode(value)
    }

    fun toggleShowNsfw(value: Boolean) = viewModelScope.launch {
        repo.setShowNsfw(value)
    }

    fun toggleBlurNsfw(value: Boolean) = viewModelScope.launch {
        repo.setBlurNsfw(value)
    }

    fun setAutoplayVideo(value: AutoplayVideo) = viewModelScope.launch {
        repo.setAutoplayVideo(value)
    }

    fun setDefaultSort(sort: PostSort) = viewModelScope.launch {
        repo.setDefaultSort(sort)
    }
}
