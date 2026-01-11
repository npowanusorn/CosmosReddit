package com.hamburghini.cosmos.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.hamburghini.cosmos.model.AuthState
import com.hamburghini.cosmos.ui.theme.RedditOrange
import com.hamburghini.cosmos.util.PostUtils
import com.hamburghini.cosmos.util.findActivity
import com.hamburghini.cosmos.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val storedAccounts by viewModel.storedAccounts.collectAsState()
    val context = LocalContext.current
    val activity = context.findActivity()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            when (authState) {
                is AuthState.NotLoggedIn -> {
                    NotLoggedInHeader(
                        onLoginClick = {
                            activity?.let { viewModel.startLogin(it) }
                                ?: run { /* Handle case where activity is null */ }
                        }
                    )
                }
                is AuthState.LoggingIn -> {
                    LoggingInHeader()
                }
                is AuthState.LoggedIn -> {
                    val loggedInAuthState = authState as AuthState.LoggedIn
                    LoggedInHeader(
                        authState = loggedInAuthState,
                        onLogoutClick = { viewModel.logout() },
                        onSwitchAccount = { account -> viewModel.switchAccount(account) }
                    )
                }
                is AuthState.AuthError -> {
                    val errorAuthState = authState as AuthState.AuthError
                    AuthErrorHeader(
                        error = errorAuthState.error,
                        canRetry = errorAuthState.canRetry,
                        onRetryClick = {
                            activity?.let { viewModel.retryLogin(it) }
                                ?: run { /* Handle case where activity is null */ }
                        },
                        onLoginClick = {
                            activity?.let { viewModel.startLogin(it) }
                                ?: run { /* Handle case where activity is null */ }
                        }
                    )
                }
            }
        }

        // Show stored accounts if any exist
        if (storedAccounts.isNotEmpty()) {
            item {
                StoredAccountsSection(
                    accounts = storedAccounts,
                    currentAccount = (authState as? AuthState.LoggedIn)?.account,
                    onSwitchAccount = { account -> viewModel.switchAccount(account) },
                    onRemoveAccount = { account -> viewModel.removeAccount(account) }
                )
            }
        }

        // Show profile stats if logged in
        if (authState is AuthState.LoggedIn) {
            val loggedInAuthState = authState as AuthState.LoggedIn
            item {
                ProfileStats(loggedInAuthState.userInfo.linkKarma, loggedInAuthState.userInfo.commentKarma)
            }

            item {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Mock recent activity - replace with real data
            items(5) { index ->
                ActivityCard(
                    title = "Commented on: Cool Android feature",
                    subreddit = "r/androiddev",
                    timestamp = "${index + 1}h ago"
                )
            }
        } else {
            item {
                AnonymousFeaturesList()
            }
        }
    }
}

@Composable
private fun NotLoggedInHeader(
    onLoginClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Anonymous avatar
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Anonymous user",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(80.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Anonymous",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Log in to access personalized features",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedditOrange
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Login,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log in with Reddit")
            }
        }
    }
}

@Composable
private fun LoggingInHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = RedditOrange,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "Logging in...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LoggedInHeader(
    authState: AuthState.LoggedIn,
    onLogoutClick: () -> Unit,
    onSwitchAccount: (com.hamburghini.cosmos.model.RedditAccount) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User avatar
            if (authState.userInfo.iconImg?.isNotBlank() == true) {
                AsyncImage(
                    model = authState.userInfo.iconImg,
                    contentDescription = "User avatar",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(RedditOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = authState.account.username.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "u/${authState.account.username}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Redditor since ${PostUtils.formatTimeAgo(authState.userInfo.createdUtc)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log out")
            }
        }
    }
}

@Composable
private fun AuthErrorHeader(
    error: String,
    canRetry: Boolean,
    onRetryClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Authentication Failed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (canRetry) {
                    OutlinedButton(onClick = onRetryClick) {
                        Text("Retry")
                    }
                }
                Button(
                    onClick = onLoginClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedditOrange
                    )
                ) {
                    Text("Try Again")
                }
            }
        }
    }
}

@Composable
private fun StoredAccountsSection(
    accounts: List<com.hamburghini.cosmos.model.RedditAccount>,
    currentAccount: com.hamburghini.cosmos.model.RedditAccount?,
    onSwitchAccount: (com.hamburghini.cosmos.model.RedditAccount) -> Unit,
    onRemoveAccount: (com.hamburghini.cosmos.model.RedditAccount) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Accounts",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        accounts.forEach { account ->
            AccountCard(
                account = account,
                isActive = currentAccount?.username == account.username,
                onSwitchClick = { onSwitchAccount(account) },
                onRemoveClick = { onRemoveAccount(account) }
            )
        }
    }
}

@Composable
private fun AccountCard(
    account: com.hamburghini.cosmos.model.RedditAccount,
    isActive: Boolean,
    onSwitchClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isActive) RedditOrange else MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = account.username.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "u/${account.username}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (isActive) {
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (!isActive) {
                Row {
                    OutlinedButton(
                        onClick = onSwitchClick,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Switch")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStats(
    linkKarma: Int,
    commentKarma: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "Post Karma", value = PostUtils.formatScore(linkKarma))
            StatItem(label = "Comment Karma", value = PostUtils.formatScore(commentKarma))
            StatItem(label = "Total", value = PostUtils.formatScore(linkKarma + commentKarma))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActivityCard(
    title: String,
    subreddit: String,
    timestamp: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subreddit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AnonymousFeaturesList() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Browse Reddit Anonymously",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "You can still browse posts and communities without logging in. " +
                        "Sign in to unlock features like voting, commenting, and personalized feeds.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("✓ Browse popular posts", style = MaterialTheme.typography.bodyMedium)
                Text("✓ Explore communities", style = MaterialTheme.typography.bodyMedium)
                Text("✓ Search content", style = MaterialTheme.typography.bodyMedium)
                Text("⚬ Vote and comment (requires login)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("⚬ Create posts (requires login)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("⚬ Messages (requires login)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}