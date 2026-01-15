package com.hamburghini.cosmos.ui.screens.settings

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.hamburghini.cosmos.core.constants.AppTheme
import com.hamburghini.cosmos.ui.components.SettingsDropdown
import com.hamburghini.cosmos.ui.components.SettingsSwitch

@Composable
fun AppearanceSettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val theme by viewModel.theme.collectAsState()
    val compactMode by viewModel.compactMode.collectAsState()

    LazyColumn {

        item {
            SettingsDropdown(
                title = "Theme",
                value = theme.name,
                options = AppTheme.entries.map { it.name },
                onSelected = { viewModel.setTheme(AppTheme.valueOf(it)) }
            )
        }

        item {
            SettingsSwitch(
                title = "Compact mode",
                subtitle = "Show more posts on screen",
                checked = compactMode,
                onCheckedChange = viewModel::toggleCompactMode
            )
        }
    }
}
