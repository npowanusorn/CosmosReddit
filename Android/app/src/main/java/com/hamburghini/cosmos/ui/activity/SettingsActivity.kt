package com.hamburghini.cosmos.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hamburghini.cosmos.ui.components.RedditTopAppBar
import com.hamburghini.cosmos.ui.screens.settings.SettingsNavGraph
import com.hamburghini.cosmos.ui.screens.settings.SettingsRoute
import com.hamburghini.cosmos.ui.theme.CosmosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CosmosTheme {
                val navController = rememberNavController()
                val currentRoute = navController
                    .currentBackStackEntryAsState()
                    .value
                    ?.destination
                    ?.route

                val title = when (currentRoute) {
                    SettingsRoute.Appearance.route -> "Appearance"
                    SettingsRoute.Content.route -> "Content"
                    SettingsRoute.Media.route -> "Media"
                    SettingsRoute.About.route -> "About"
                    else -> "Settings"
                }


                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        RedditTopAppBar(
                            title = title,
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        if (!navController.popBackStack()) {
                                            finish()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                    )
                                }
                            },
                            isCenter = true
                        )
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        SettingsNavGraph(
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}