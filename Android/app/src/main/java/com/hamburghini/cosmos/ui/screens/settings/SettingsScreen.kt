package com.hamburghini.cosmos.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun SettingsScreen(
    navController: NavController
) {
    LazyColumn {

        item {
            SettingsCategoryItem(
                title = "Appearance",
                subtitle = "Theme, layout",
                onClick = {
                    navController.navigate(SettingsRoute.Appearance.route)
                }
            )
        }

        item {
            SettingsCategoryItem(
                title = "Content",
                subtitle = "NSFW, sorting",
                onClick = {
                    navController.navigate(SettingsRoute.Content.route)
                }
            )
        }

        item {
            SettingsCategoryItem(
                title = "Media",
                subtitle = "Autoplay, sound",
                onClick = {
                    navController.navigate(SettingsRoute.Media.route)
                }
            )
        }

        item {
            SettingsCategoryItem(
                title = "About",
                subtitle = "Version, licenses",
                onClick = {
                    navController.navigate(SettingsRoute.About.route)
                }
            )
        }
    }
}

@Composable
fun SettingsCategoryItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) }
    )
}
