package com.hamburghini.cosmos.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.hamburghini.cosmos.core.auth.RedditAuthManager
import com.hamburghini.cosmos.data.manager.ProfileManager
import com.hamburghini.cosmos.ui.screens.MainScreen
import com.hamburghini.cosmos.ui.theme.CosmosTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: RedditAuthManager

    @Inject
    lateinit var profileManager: ProfileManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle OAuth callback if this activity was launched by the redirect
        handleAuthCallbackIfNeeded(intent)

        setContent {
            CosmosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onLoginClick = { handleLogin() },
                        onSettingsClick = { handleSettings() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle OAuth callback for singleTop launch mode
        handleAuthCallbackIfNeeded(intent)
    }

    private fun handleAuthCallbackIfNeeded(intent: Intent) {
        val uri = intent.data
        if (uri != null && uri.scheme == "mysimprc" && uri.host == "auth") {
            // This is an OAuth callback
            lifecycleScope.launch {
                try {
                    when (val result = authManager.handleAuthCallback(intent)) {
                        is RedditAuthManager.AuthResult.Success -> {
                            // Complete login in ProfileManager
                            profileManager.completeLogin(result.account, result.userInfo)
                        }
                        is RedditAuthManager.AuthResult.Error -> {
                            // Handle login error
                            profileManager.handleLoginError(result.message)
                        }
                    }
                } catch (e: Exception) {
                    profileManager.handleLoginError("Authentication failed: ${e.message}")
                }
            }
        }
    }

    /**
     * Start OAuth login flow - this will open Chrome Custom Tab
     * Requires Activity context to launch Chrome Custom Tab
     */
    private fun handleLogin() {
        profileManager.startLogin(this)
    }

    private fun handleSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}