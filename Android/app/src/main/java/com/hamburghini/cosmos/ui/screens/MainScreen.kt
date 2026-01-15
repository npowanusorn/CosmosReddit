package com.hamburghini.cosmos.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.hamburghini.cosmos.R
import com.hamburghini.cosmos.model.AuthState
import com.hamburghini.cosmos.core.navigation.BottomNavDestination
import com.hamburghini.cosmos.ui.components.CustomBottomTabBar
import com.hamburghini.cosmos.ui.components.ProfileSwitcherBottomSheet
import com.hamburghini.cosmos.ui.components.RedditTopAppBar
import com.hamburghini.cosmos.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLoginClick: () -> Unit,
    onSettingsClick: () -> Unit
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
                actions = {
                    if (currentTab == BottomNavDestination.PROFILE) {
                        IconButton(onClick = {
                            println("onSettingsClick")
                            onSettingsClick()
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = stringResource(R.string.settings),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        // Search button
                        IconButton(onClick = {
                            println("onSearchClick")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.search),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Notifications button
                        IconButton(onClick = {
                            if (authState is AuthState.LoggedIn) {
                                println("onNotificationClick for user: ${(authState as AuthState.LoggedIn).account.username}")
                            } else {
                                println("Notifications require login")
                                // Could show a login prompt here
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = stringResource(R.string.notifications),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }
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