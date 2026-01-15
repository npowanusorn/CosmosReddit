package com.hamburghini.cosmos.ui.screens.settings

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.hamburghini.cosmos.core.constants.PostSort
import com.hamburghini.cosmos.ui.components.SettingsDropdown
import com.hamburghini.cosmos.ui.components.SettingsSwitch

@Composable
fun ContentSettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val showNsfw by viewModel.showNsfw.collectAsState()
    val blurNsfw by viewModel.blurNsfw.collectAsState()
    val defaultSort by viewModel.defaultSort.collectAsState()

    LazyColumn {

        item {
            SettingsSwitch(
                title = "Show NSFW content",
                checked = showNsfw,
                onCheckedChange = viewModel::toggleShowNsfw
            )
        }

        item {
            SettingsSwitch(
                title = "Blur NSFW thumbnails",
                checked = blurNsfw,
                onCheckedChange = viewModel::toggleBlurNsfw
            )
        }

        item {
            SettingsDropdown(
                title = "Default post sort",
                value = defaultSort.name,
                options = PostSort.entries.map { it.name },
                onSelected = { viewModel.setDefaultSort(PostSort.valueOf(it)) }
            )
        }
    }
}
