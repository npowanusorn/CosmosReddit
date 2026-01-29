package com.hamburghini.cosmos.ui.screens.settings

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
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
        startDestination = SettingsRoute.Root.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
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
