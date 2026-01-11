package com.hamburghini.cosmos.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.hamburghini.cosmos.R
import com.hamburghini.cosmos.model.AuthState
import com.hamburghini.cosmos.navigation.BottomNavDestination
import com.hamburghini.cosmos.ui.components.CustomBottomTabBar
import com.hamburghini.cosmos.ui.components.ProfileSwitcherBottomSheet
import com.hamburghini.cosmos.ui.components.RedditTopAppBar
import com.hamburghini.cosmos.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLoginClick: () -> Unit
) {
    var currentTab by remember { mutableStateOf(BottomNavDestination.HOME) }
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val authState by profileViewModel.authState.collectAsState()

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

    var showProfileSwitcher by remember { mutableStateOf(false) }

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
                    if (authState is AuthState.LoggedIn) {
                        println("onNotificationClick for user: ${(authState as AuthState.LoggedIn).account.username}")
                    } else {
                        println("Notifications require login")
                        // Could show a login prompt here
                    }
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
                onProfileLongClick = {
                    // Only show profile switcher if user is logged in or has stored accounts
                    if (authState is AuthState.LoggedIn || profileViewModel.storedAccounts.value.isNotEmpty()) {
                        showProfileSwitcher = true
                    }
                }
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
                BottomNavDestination.CHAT -> ChatScreen(profileViewModel)
                BottomNavDestination.PROFILE -> ProfileScreen(
                    onLoginClick = onLoginClick,
                    viewModel = profileViewModel
                )
            }
        }

        // Profile Switcher Bottom Sheet
        if (showProfileSwitcher) {
            ProfileSwitcherBottomSheet(
                onDismissRequest = { showProfileSwitcher = false },
                onAddAccountClick = {
                    showProfileSwitcher = false
                    onLoginClick()
                },
                viewModel = profileViewModel
            )
        }
    }
}