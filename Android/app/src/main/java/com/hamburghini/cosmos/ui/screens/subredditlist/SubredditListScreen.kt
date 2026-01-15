package com.hamburghini.cosmos.ui.screens.subredditlist

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.hamburghini.cosmos.R
import com.hamburghini.cosmos.model.AuthState
import com.hamburghini.cosmos.model.SubredditAboutData
import com.hamburghini.cosmos.ui.theme.RedditOrange
import com.hamburghini.cosmos.core.util.Logger
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubredditListScreen(
    viewModel: SubredditListViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val mySubreddits by viewModel.mySubreddits.collectAsState()
    val favoriteSubreddits by viewModel.favoriteSubreddits.collectAsState()
    val popularSubreddits by viewModel.popularSubreddits.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val showScrollToTopFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 2
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

                // Favorites Section (only if logged in and has favorites)
                if (authState is AuthState.LoggedIn && favoriteSubreddits.isNotEmpty() && mySubreddits.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Favorites",
                            count = favoriteSubreddits.size,
                        )
                    }

                    items(
                        items = favoriteSubreddits,
                        key = { "favorite_$it" }
                    ) { favorite ->
                        val favoriteSubreddit = mySubreddits.first { it.name == favorite }
                        SubredditCard(
                            subreddit = favoriteSubreddit,
                            isSubscribed = true,
                            isFavorited = true,
                            canInteract = true,
                            onFavoriteClick = {},
                            onSubredditClick = {}
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // My Subreddits Section (only if logged in)
                if (authState is AuthState.LoggedIn) {
                    item {
                        SectionHeader(
                            title = "My Communities",
                            count = mySubreddits.size,
                            showLoadAll = uiState.hasMoreMy,
                            isLoading = uiState.isLoadingMy,
                            onLoadAllClick = {
                                viewModel.loadMySubscriptions(true)
                            }
                        )
                    }

                    // Show error if any
                    if (uiState.errorMy != null) {
                        item {
                            ErrorCard(
                                error = uiState.errorMy ?: "",
                                onRetry = { viewModel.loadMySubscriptions(true) },
                                onDismiss = { viewModel.clearErrors() }
                            )
                        }
                    }

                    // Show my subreddits
                    items(
                        items = mySubreddits,
                        key = { it.name }
                    ) { subreddit ->
                        Logger.i("$subreddit")
                        SubredditCard(
                            subreddit = subreddit,
                            isSubscribed = true,
                            isFavorited = viewModel.isFavorited(subreddit.name),
                            canInteract = true,
                            onFavoriteClick = {
                                viewModel.toggleFavorite(subreddit.name)
                            },
                            onSubredditClick = {}
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
                } else {
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

                    // Show popular subreddits
                    items(
                        items = popularSubreddits,
                        key = { it.name }
                    ) { subreddit ->
                        SubredditCard(
                            subreddit = subreddit,
                            isSubscribed = false,
                            isFavorited = false,
                            canInteract = false,
                            onFavoriteClick = {},
                            onSubredditClick = {}
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
    count: Int,
    showLoadAll: Boolean = false,
    isLoading: Boolean = false,
    onLoadAllClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (count > 0) {
                    Text(
                        text = "$count communities",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (showLoadAll && !isLoading) {
            OutlinedButton(
                onClick = onLoadAllClick,
                modifier = Modifier.height(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Load All", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun SubredditCard(
    subreddit: SubredditAboutData,
    isSubscribed: Boolean,
    isFavorited: Boolean,
    canInteract: Boolean,
    onFavoriteClick: () -> Unit,
    onSubredditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onSubredditClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                    text = subreddit.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (canInteract) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Favorite button (only for logged in users)
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            painter = painterResource(if (isFavorited) R.drawable.ic_star_filled else R.drawable.ic_star),
                            contentDescription = if (isFavorited) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorited) RedditOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
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

@Preview
@Composable
fun PreviewSubredditCard() {
    SubredditCard(
        subreddit = SubredditAboutData.mock,
        isSubscribed = true,
        isFavorited = true,
        canInteract = true,
        onSubredditClick = {},
        onFavoriteClick = {}
    )
}