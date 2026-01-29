package com.hamburghini.cosmos.ui.screens.home

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.hamburghini.cosmos.core.constants.Constants
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.ui.activity.PostDetailActivity
import com.hamburghini.cosmos.ui.activity.VideoPlayerActivity
import com.hamburghini.cosmos.ui.components.PostCard
import com.hamburghini.cosmos.ui.components.PostMenuBottomSheet
import com.hamburghini.cosmos.ui.theme.RedditOrange
import com.hamburghini.cosmos.core.util.Logger
import com.hamburghini.cosmos.core.util.PostUtils
import com.hamburghini.cosmos.data.repository.LoadType
import com.hamburghini.cosmos.data.repository.PostsState
import com.hamburghini.cosmos.data.repository.SortType
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val postsState by viewModel.postsState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val pullToRefreshState = rememberPullToRefreshState()
    val isRefreshing by remember {
        derivedStateOf { postsState is PostsState.Refresh }
    }

    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var showPostMenu by remember { mutableStateOf(false) }

    val showScrollToTopFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 1
        }
    }

    // Load more posts when reaching the end
    LaunchedEffect(listState) {
        snapshotFlow {
            val lastItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            Pair(lastItem?.index, listState.layoutInfo.totalItemsCount)
        }
            .distinctUntilChanged()
            .collect { (lastIndex, total) ->
                if (lastIndex != null && postsState is PostsState.Success) {
                    val hasMore = (postsState as PostsState.Success).currentAfter != null
                    if (hasMore && lastIndex >= (total - Constants.LOAD_MORE_BUFFER)) {
                        viewModel.loadPosts(LoadType.MORE)
                    }
                }
            }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

            layoutInfo.visibleItemsInfo.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - viewportCenter)
            }?.index
        }
            .distinctUntilChanged()
            .collect { focusedIndex ->
                focusedIndex?.let {
                    Logger.d("TODO - auto play post at: $it")
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            state = pullToRefreshState,
            onRefresh = { viewModel.loadPosts(LoadType.REFRESH) },
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            },
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

                val content: PostsState? = when (postsState) {
                    is PostsState.Error -> (postsState as PostsState.Error).previous
                    is PostsState.LoadingMore -> (postsState as PostsState.LoadingMore).previous
                    is PostsState.Refresh -> (postsState as PostsState.Refresh).previous
                    is PostsState.Success -> postsState
                    else -> null
                }

                if (content != null) {
                    val posts = (content as PostsState.Success).posts
                    items(
                        items = posts.values.toList(),
                        key = { post -> post.name }
                    ) { post ->
                        PostCard(
                            post = post,
                            onPostClick = { clickedPost ->
                                println("Post clicked: ${clickedPost.title}")
                                context.startActivity(
                                    Intent(context, PostDetailActivity::class.java).apply {
                                        putExtra(Constants.CLICKED_POST_NAME, clickedPost.name)
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
                                val width = post.preview?.videoPreview?.width?.toFloat()
                                val height = post.preview?.videoPreview?.height?.toFloat()
                                val aspectRatio = if (width != null && height != null && height != 0F) {
                                    width / height
                                } else {
                                    null
                                }
                                Logger.i("videoUrl: $videoUrl, aspectRatio: $aspectRatio")
                                if (!videoUrl.isNullOrEmpty()) {
                                    context.startActivity(
                                        Intent(context, VideoPlayerActivity::class.java).apply {
                                            putExtra(Constants.VIDEO_CLICKED_PARCELABLE, videoUrl)
                                            putExtra(Constants.CLICKED_VIDEO_ASPECT_RATIO, aspectRatio)
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
                                LoadingIndicator(
                                    color = RedditOrange
                                )
                            }
                        }
                    }
                } else {
                    when (postsState) {
                        is PostsState.Error -> {
                            val errorState = postsState as PostsState.Error
                            item {
                                ErrorMessage(
                                    error = errorState.message,
                                    onRetry = { viewModel.loadPosts(LoadType.MORE) },
                                    onDismiss = { viewModel.clearError() }
                                )
                            }
                        }
                        is PostsState.InitialLoading, PostsState.Idle -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LoadingIndicator(
                                        color = RedditOrange
                                    )
                                }
                            }
                        }
                        else -> Unit
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
                    PostUtils.sharePost(context, post)
                }
            },
            onCopyLinkClick = {
                currentPost.let { post ->
                    PostUtils.copyPostLink(context, post)
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