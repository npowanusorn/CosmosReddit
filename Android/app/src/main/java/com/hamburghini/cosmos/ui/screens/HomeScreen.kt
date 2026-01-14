package com.hamburghini.cosmos.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.hamburghini.cosmos.Constants
import com.hamburghini.cosmos.model.Post
import com.hamburghini.cosmos.ui.activity.PostDetailActivity
import com.hamburghini.cosmos.ui.activity.VideoPlayerActivity
import com.hamburghini.cosmos.ui.components.PostCard
import com.hamburghini.cosmos.ui.components.PostMenuBottomSheet
import com.hamburghini.cosmos.ui.theme.RedditOrange
import com.hamburghini.cosmos.util.Logger
import com.hamburghini.cosmos.viewmodel.HomeViewModel
import com.hamburghini.cosmos.viewmodel.SortType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var showPostMenu by remember { mutableStateOf(false) }

    val showScrollToTopFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 1
        }
    }

    // Load more posts when reaching the end
    LaunchedEffect(listState) {
        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        val totalItems = listState.layoutInfo.totalItemsCount

        if (lastVisibleIndex != null &&
            lastVisibleIndex >= totalItems - 3 &&
            !uiState.isLoading &&
            !uiState.isLoadingMore &&
            uiState.hasMore) {
            viewModel.loadMorePosts()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refreshPosts() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SortFilterRow(
                        currentSort = uiState.currentSortType,
                        isPersonalized = uiState.isPersonalized,
                        onSortChanged = { sortType ->
                            viewModel.loadPostsForSort(sortType)
                        }
                    )
                }

                if (uiState.error != null && posts.isEmpty()) {
                    item {
                        ErrorMessage(
                            error = "${uiState.error}",
                            onRetry = { viewModel.refreshPosts() },
                            onDismiss = { viewModel.clearError() }
                        )
                    }
                } else {
                    items(
                        items = posts,
                        key = { post -> post.id }
                    ) { post ->
                        PostCard(
                            post = post,
                            onPostClick = { clickedPost ->
                                println("Post clicked: ${clickedPost.title}")
                                context.startActivity(
                                    Intent(context, PostDetailActivity::class.java).apply {
                                        putExtra(Constants.CLICKED_POST_PARCELABLE, clickedPost)
                                    }
                                )
                            },
                            onVote = { postId, direction ->
                                viewModel.voteOnPost(postId, direction)
                            },
                            onMenuClick = { clickedPost ->
                                selectedPost = clickedPost
                                showPostMenu = true
                            },
                            onSubredditClick = { post ->
                                Logger.i("onSubredditClick: ${post.subreddit_id}")
                            },
                            onAuthorClick = { post ->
                                Logger.i("onAuthorClick: ${post.author}")
                            },
                            onVideoClick = {
                                val videoUrl = post.preview?.videoPreview?.dashUrl
                                Logger.i("videoUrl: $videoUrl")
                                if (!videoUrl.isNullOrEmpty()) {
                                    context.startActivity(
                                        Intent(context, VideoPlayerActivity::class.java).apply {
                                            putExtra(Constants.VIDEO_CLICKED_PARCELABLE, videoUrl)
                                        }
                                    )
                                }
                            }
                        )
                    }

                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = RedditOrange
                                )
                            }
                        }
                    }
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )

        // Floating Action Button for scroll to top
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

    // Post Menu Bottom Sheet
    if (showPostMenu && selectedPost != null) {
        // Get the latest post state from the view model
        val currentPost = viewModel.getPost(selectedPost!!.name) ?: selectedPost!!

        PostMenuBottomSheet(
            post = currentPost,
            isLoggedIn = viewModel.isLoggedIn(),
            onDismissRequest = {
                showPostMenu = false
                selectedPost = null
            },
            onSaveClick = {
                currentPost.let { post ->
                    // Toggle save state
                    val newSaveState = !post.saved
                    viewModel.savePost(post.name, newSaveState)

                    // Update selected post immediately for UI feedback
                    selectedPost = post.copy(saved = newSaveState)

                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            if (newSaveState) "Post saved" else "Post unsaved"
                        )
                    }
                }
            },
            onHideClick = {
                currentPost.let { post ->
                    // TODO: Implement hide functionality
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            if (post.hidden == true) "TODO: Post unhidden" else "TODO: Post hidden"
                        )
                    }
                }
            },
            onReportClick = {
                currentPost.let { post ->
                    // TODO: Navigate to report screen
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Report functionality coming soon")
                    }
                }
            },
            onShareClick = {
                currentPost.let { post ->
                    sharePost(context, post)
                }
            },
            onCopyLinkClick = {
                currentPost.let { post ->
                    copyPostLink(context, post)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Link copied to clipboard")
                    }
                }
            },
            onBlockUserClick = {
                currentPost.let { post ->
                    // TODO: Implement block user functionality
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Block user functionality coming soon")
                    }
                }
            },
            onViewProfileClick = {
                currentPost.let { post ->
                    // TODO: Navigate to user profile
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Navigate to u/${post.author}")
                    }
                }
            },
            onViewSubredditClick = {
                currentPost.let { post ->
                    // TODO: Navigate to subreddit
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Navigate to ${post.subreddit_name_prefixed}")
                    }
                }
            }
        )
    }

}

@Composable
private fun SortFilterRow(
    currentSort: SortType,
    isPersonalized: Boolean,
    onSortChanged: (SortType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Show appropriate sorts based on whether it's personalized feed
        val availableSorts = if (isPersonalized) {
            listOf(SortType.BEST, SortType.HOT, SortType.NEW, SortType.TOP)
        } else {
            SortType.entries
        }

        items(availableSorts) { sortType ->
            FilterChip(
                selected = currentSort == sortType,
                onClick = { onSortChanged(sortType) },
                label = {
                    Text(
                        text = when (sortType) {
                            SortType.BEST -> "Best"
                            SortType.HOT -> "Hot"
                            SortType.NEW -> "New"
                            SortType.TOP -> "Top"
                            SortType.RISING -> "Rising"
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RedditOrange,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun ErrorMessage(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Oops! Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
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
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text("Retry")
            }
        }
    }
}

private fun sharePost(context: Context, post: Post) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, post.title)
        putExtra(Intent.EXTRA_TEXT, "https://reddit.com${post.permalink}")
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share post via"))
}

private fun copyPostLink(context: Context, post: Post) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Reddit post link", "https://reddit.com${post.permalink}")
    clipboard.setPrimaryClip(clip)
}