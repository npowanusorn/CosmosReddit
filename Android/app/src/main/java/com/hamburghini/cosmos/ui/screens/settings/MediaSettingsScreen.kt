package com.hamburghini.cosmos.ui.screens.settings

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.hamburghini.cosmos.core.constants.AutoplayVideo
import com.hamburghini.cosmos.ui.components.SettingsDropdown

@Composable
fun MediaSettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val autoplayVideo by viewModel.autoplayVideo.collectAsState()

    LazyColumn {
        item {
            SettingsDropdown(
                title = "Autoplay videos",
                value = autoplayVideo.name,
                options = AutoplayVideo.entries.map { it.name },
                onSelected = {
                    viewModel.setAutoplayVideo(AutoplayVideo.valueOf(it))
                }
            )
        }
    }
}
