package com.hamburghini.cosmos.ui.screens.subredditlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.hamburghini.cosmos.R
import com.hamburghini.cosmos.data.model.AuthState
import com.hamburghini.cosmos.data.model.SubredditAboutData
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

    // Build index sections for logged-in users
    val indexSections = remember(mySubreddits, favoriteSubreddits, authState) {
        if (authState is AuthState.LoggedIn && mySubreddits.isNotEmpty()) {
            buildIndexSections(
                hasFavorites = favoriteSubreddits.isNotEmpty(),
                subreddits = mySubreddits,
                favoriteCount = favoriteSubreddits.size
            )
        } else {
            emptyList()
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
                    end = if (indexSections.isNotEmpty()) 40.dp else 16.dp, // Extra padding for index bar
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
                            title = "Subscribed",
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
                    // Not logged in header
                    item {
                        NotLoggedInHeader(onLoginClick = {})
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

                    // Show popular subreddits
                    items(
                        items = popularSubreddits,
                        key = { it.name }
                    ) { subreddit ->
                        val isSubscribed = viewModel.isSubscribed(subreddit.name)
                        SubredditCard(
                            subreddit = subreddit,
                            isSubscribed = isSubscribed,
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

                    // Empty state
                    if (!uiState.isLoadingPopular && popularSubreddits.isEmpty() && uiState.errorPopular == null) {
                        item {
                            EmptyPopularCard()
                        }
                    }
                }
            }
        }

        // A-Z Index Bar (only for logged-in users with subscriptions)
        if (authState is AuthState.LoggedIn && indexSections.isNotEmpty()) {
            IndexBar(
                sections = indexSections,
                onSectionSelected = { section ->
                    coroutineScope.launch {
                        listState.animateScrollToItem(section.firstItemIndex)
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(0.7f)
                    .padding(end = 4.dp)
            )
        }

        // Scroll to top FAB
        AnimatedVisibility(
            visible = showScrollToTopFab,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
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
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Scroll to top",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun IndexBar(
    sections: List<IndexSection>,
    onSectionSelected: (IndexSection) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var showLabel by remember { mutableStateOf(false) }
    var barSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    Box(modifier = modifier) {
        // Index labels
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .onGloballyPositioned { coordinates ->
                    barSize = coordinates.size
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            showLabel = true
                            val index = ((offset.y / barSize.height) * sections.size).toInt()
                                .coerceIn(0, sections.lastIndex)
                            selectedIndex = index
                            onSectionSelected(sections[index])
                        },
                        onDrag = { change, _ ->
                            val offset = change.position
                            val index = ((offset.y / barSize.height) * sections.size).toInt()
                                .coerceIn(0, sections.lastIndex)
                            if (index != selectedIndex) {
                                selectedIndex = index
                                onSectionSelected(sections[index])
                            }
                        },
                        onDragEnd = {
                            showLabel = false
                            selectedIndex = -1
                        },
                        onDragCancel = {
                            showLabel = false
                            selectedIndex = -1
                        }
                    )
                }
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            sections.forEachIndexed { index, section ->
                Text(
                    text = section.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (index == selectedIndex) {
                        RedditOrange
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }

        // Selected label popup
        if (showLabel && selectedIndex in sections.indices) {
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 32.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp
            ) {
                Text(
                    text = sections[selectedIndex].label,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    showLoadAll: Boolean = false,
    isLoading: Boolean = false,
    onLoadAllClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$count communities",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (showLoadAll && !isLoading) {
            OutlinedButton(
                onClick = onLoadAllClick,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Load all", style = MaterialTheme.typography.labelMedium)
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
    onSubredditClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onSubredditClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Icon + Info
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subreddit icon
                AsyncImage(
                    model = subreddit.communityIcon?.ifBlank { subreddit.iconImg },
                    contentDescription = "${subreddit.displayName} icon",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.ic_reddit_icon),
                    placeholder = painterResource(R.drawable.ic_reddit_icon)
                )

                // Subreddit info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = subreddit.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right side: Action buttons
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingMoreIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator(
            color = RedditOrange,
            modifier = Modifier.size(24.dp),
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

@Composable
private fun EmptyPopularCard() {
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
                text = "No Communities Found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Check your connection and try again",
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