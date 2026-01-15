package com.hamburghini.cosmos.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hamburghini.cosmos.BuildConfig

@Composable
fun AboutSettingsScreen() {
    LazyColumn {
        item {
            ListItem(
                headlineContent = { Text("Version") },
                supportingContent = { Text(BuildConfig.VERSION_NAME) }
            )
        }

        item {
            ListItem(
                modifier = Modifier.clickable { /* open licenses */ },
                headlineContent = { Text("Open source licenses") }
            )
        }
    }
}
