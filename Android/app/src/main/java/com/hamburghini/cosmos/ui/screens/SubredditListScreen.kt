package com.hamburghini.cosmos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.hamburghini.cosmos.model.AuthState
import com.hamburghini.cosmos.model.SubredditAboutData
import com.hamburghini.cosmos.ui.theme.RedditOrange
import com.hamburghini.cosmos.viewmodel.SubredditListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubredditListScreen(
    onLoginClick: () -> Unit = {},
    viewModel: SubredditListViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val mySubreddits by viewModel.mySubreddits.collectAsState()
    val popularSubreddits by viewModel.popularSubreddits.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val showScrollToTopFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 2
        }
    }

    // Load more when reaching the end
    LaunchedEffect(listState) {
        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        val totalItems = listState.layoutInfo.totalItemsCount

        if (lastVisibleIndex != null && lastVisibleIndex >= totalItems - 3) {
            if (authState is AuthState.LoggedIn &&
                !uiState.isLoadingMoreMy &&
                uiState.hasMoreMy &&
                lastVisibleIndex < mySubreddits.size + 5) { // Check if we're in the my subreddits section
                viewModel.loadMoreMySubreddits()
            } else if (!uiState.isLoadingMorePopular && uiState.hasMorePopular) {
                viewModel.loadMorePopularSubreddits()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = uiState.isLoadingMy || uiState.isLoadingPopular,
            onRefresh = { viewModel.refreshAll() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show login header if not logged in
                if (authState !is AuthState.LoggedIn) {
                    item {
                        NotLoggedInHeader(onLoginClick = onLoginClick)
                    }
                }

                // My Subreddits Section (only if logged in)
                if (authState is AuthState.LoggedIn) {
                    item {
                        SectionHeader(
                            title = "My Communities",
                            count = mySubreddits.size
                        )
                    }

                    // Show error if any
                    if (uiState.errorMy != null) {
                        item {
                            ErrorCard(
                                error = uiState.errorMy ?: "",
                                onRetry = { viewModel.loadMySubreddits(forceRefresh = true) },
                                onDismiss = { viewModel.clearErrors() }
                            )
                        }
                    }

                    // Show loading for first load
                    if (uiState.isLoadingMy && mySubreddits.isEmpty()) {
                        item {
                            LoadingCard()
                        }
                    }

                    // Show my subreddits
                    items(
                        items = mySubreddits,
                        key = { it.name }
                    ) { subreddit ->
                        SubredditCard(
                            subreddit = subreddit,
                            isSubscribed = true,
                            canInteract = true,
                            onSubscribeClick = {
                                viewModel.subscribeToSubreddit(subreddit.name, false)
                            }
                        )
                    }

                    // Show loading more indicator
                    if (uiState.isLoadingMoreMy) {
                        item {
                            LoadingMoreIndicator()
                        }
                    }

                    // Empty state
                    if (!uiState.isLoadingMy && mySubreddits.isEmpty() && uiState.errorMy == null) {
                        item {
                            EmptySubscriptionsCard()
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Popular Subreddits Section
                item {
                    SectionHeader(
                        title = "Popular Communities",
                        count = popularSubreddits.size
                    )
                }

                // Show error if any
                if (uiState.errorPopular != null) {
                    item {
                        ErrorCard(
                            error = uiState.errorPopular ?: "",
                            onRetry = { viewModel.loadPopularSubreddits(forceRefresh = true) },
                            onDismiss = { viewModel.clearErrors() }
                        )
                    }
                }

                // Show loading for first load
                if (uiState.isLoadingPopular && popularSubreddits.isEmpty()) {
                    item {
                        LoadingCard()
                    }
                }

                // Show popular subreddits
                items(
                    items = popularSubreddits,
                    key = { it.name }
                ) { subreddit ->
                    val isSubscribed = if (authState is AuthState.LoggedIn) {
                        viewModel.isSubscribed(subreddit.name)
                    } else {
                        false
                    }

                    SubredditCard(
                        subreddit = subreddit,
                        isSubscribed = isSubscribed,
                        canInteract = authState is AuthState.LoggedIn,
                        onSubscribeClick = {
                            viewModel.subscribeToSubreddit(subreddit.name, !isSubscribed)
                        }
                    )
                }

                // Show loading more indicator
                if (uiState.isLoadingMorePopular) {
                    item {
                        LoadingMoreIndicator()
                    }
                }
            }
        }

        // Scroll to top FAB
        AnimatedVisibility(
            visible = showScrollToTopFab,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                containerColor = RedditOrange,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Scroll to top"
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (count > 0) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NotLoggedInHeader(onLoginClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = "Communities",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Explore Communities",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Discover popular communities and topics. " +
                            "Log in to join communities and get personalized recommendations.",
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
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Login,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log in for more features")
            }

            Text(
                text = "Continue browsing popular communities below ↓",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SubredditCard(
    subreddit: SubredditAboutData,
    isSubscribed: Boolean,
    canInteract: Boolean,
    onSubscribeClick: () -> Unit
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Community icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            ) {
                val iconUrl = subreddit.communityIcon?.takeIf { it.isNotBlank() }
                    ?: subreddit.iconImg?.takeIf { it.isNotBlank() }

                if (iconUrl != null) {
                    AsyncImage(
                        model = iconUrl,
                        contentDescription = "${subreddit.displayName} icon",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isSubscribed) RedditOrange else MaterialTheme.colorScheme.primary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subreddit.displayName.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = subreddit.displayNamePrefixed,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (subreddit.publicDescription?.isNotBlank() == true) {
                    Text(
                        text = subreddit.publicDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${formatSubscriberCount(subreddit.subscribers)} members",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (subreddit.activeUserCount != null && subreddit.activeUserCount > 0) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "${formatSubscriberCount(subreddit.activeUserCount)} online",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (canInteract) {
                if (isSubscribed) {
                    OutlinedButton(
                        onClick = onSubscribeClick,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Joined", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Button(
                        onClick = onSubscribeClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RedditOrange
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Join", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRetry,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = RedditOrange
        )
    }
}

@Composable
private fun LoadingMoreIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = RedditOrange,
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
    }
}

@Composable
private fun EmptySubscriptionsCard() {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "No Subscriptions Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Join communities below to see them here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatSubscriberCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fk", count / 1_000.0)
        else -> count.toString()
    }
}