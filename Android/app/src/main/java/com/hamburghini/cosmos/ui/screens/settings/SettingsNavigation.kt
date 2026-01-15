package com.hamburghini.cosmos.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

sealed class SettingsRoute(val route: String) {
    data object Root : SettingsRoute("settings")
    data object Appearance : SettingsRoute("settings/appearance")
    data object Content : SettingsRoute("settings/content")
    data object Media : SettingsRoute("settings/media")
    data object About : SettingsRoute("settings/about")
}

@Composable
fun SettingsNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = SettingsRoute.Root.route
    ) {

        composable(SettingsRoute.Root.route) {
            SettingsScreen(navController)
        }

        composable(SettingsRoute.Appearance.route) {
            AppearanceSettingsScreen()
        }

        composable(SettingsRoute.Content.route) {
            ContentSettingsScreen()
        }

        composable(SettingsRoute.Media.route) {
            MediaSettingsScreen()
        }

        composable(SettingsRoute.About.route) {
            AboutSettingsScreen()
        }
    }
}
