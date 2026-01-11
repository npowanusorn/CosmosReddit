package com.hamburghini.cosmos.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.hamburghini.cosmos.R
import com.hamburghini.cosmos.navigation.BottomNavDestination
import com.hamburghini.cosmos.ui.components.CustomBottomTabBar
import com.hamburghini.cosmos.ui.components.RedditTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var currentTab by remember { mutableStateOf(BottomNavDestination.HOME) }

    val bottomNavDestinations = listOf(
        BottomNavDestination.HOME,
        BottomNavDestination.SUBREDDITS_LIST,
        BottomNavDestination.CHAT,
        BottomNavDestination.PROFILE
    )

    // Get current route for title
    val currentTitle = when (currentTab) {
        BottomNavDestination.HOME -> stringResource(R.string.tab_home)
        BottomNavDestination.SUBREDDITS_LIST -> stringResource(R.string.tab_subreddit_list)
        BottomNavDestination.CHAT -> stringResource(R.string.tab_chat)
        BottomNavDestination.PROFILE -> stringResource(R.string.tab_profile)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        topBar = {
            RedditTopAppBar(
                title = currentTitle,
                showLogo = currentTab == BottomNavDestination.HOME,
                onSearchClick = {
                    println("onSearchClick")
                },
                onNotificationClick = {
                    println("onNotificationClick")
                },
            )
        },
        bottomBar = {
            CustomBottomTabBar(
                destinations = bottomNavDestinations,
                currentDestination = currentTab.route,
                onNavigate = { destination ->
                    currentTab = destination
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                BottomNavDestination.HOME -> HomeScreen()
                BottomNavDestination.SUBREDDITS_LIST -> SubredditListScreen()
                BottomNavDestination.CHAT -> ChatScreen()
                BottomNavDestination.PROFILE -> ProfileScreen()
            }
        }
    }
}