package com.hamburghini.cosmos.ui.screens.subredditdetail

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hamburghini.cosmos.core.constants.Constants
import com.hamburghini.cosmos.core.util.Logger
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.data.repository.LoadType
import com.hamburghini.cosmos.data.repository.PostsState
import com.hamburghini.cosmos.data.repository.SortType
import com.hamburghini.cosmos.ui.activity.PostDetailActivity
import com.hamburghini.cosmos.ui.activity.SubredditDetailActivity
import com.hamburghini.cosmos.ui.activity.VideoPlayerActivity
import com.hamburghini.cosmos.ui.components.ErrorMessage
import com.hamburghini.cosmos.ui.components.PostCard
import com.hamburghini.cosmos.ui.components.SortFilterRow
import com.hamburghini.cosmos.ui.theme.RedditOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SubredditPostsSection(
    postsState: PostsState,
    blurNsfw: Boolean,
    isRefreshing: Boolean,
    currentSort: SortType,
    isPersonalized: Boolean,
    onSortClick: () -> Unit,
    onRefresh: () -> Unit,
    onSortChanged: (SortType) -> Unit,
    onVote: (String, Int) -> Unit
) {

    val context = LocalContext.current
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullToRefreshState = rememberPullToRefreshState()
    val isRefreshing by remember {
        derivedStateOf { postsState is PostsState.Refresh }
    }
    val showScrollToTopFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 1
        }
    }
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var showPostMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            state = pullToRefreshState,
            onRefresh = onRefresh,
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
                        currentSort = currentSort,
                        isPersonalized = isPersonalized,
                        onSortChanged = { sortType ->
                            onSortChanged(sortType)
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
                            blurNsfw = blurNsfw,
                            onPostClick = { clickedPost ->
                                println("Post clicked: ${clickedPost.title}")
                                context.startActivity(
                                    Intent(context, PostDetailActivity::class.java).apply {
                                        putExtra(Constants.CLICKED_POST_NAME, clickedPost.name)
                                    }
                                )
                            },
                            onVote = { postId, direction ->
                                onVote(postId, direction)
                            },
                            onMenuClick = { clickedPost ->
                                selectedPost = clickedPost
                                showPostMenu = true
                            },
                            onSubredditClick = { post ->
                                Logger.i("onSubredditClick: ${post.subreddit}")
                                context.startActivity(
                                    Intent(context, SubredditDetailActivity::class.java).apply {
                                        putExtra(SubredditDetailActivity.SUBREDDIT_NAME, post.subreddit)
                                    }
                                )
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

//                    if (uiState.isLoadingMore) {
//                        item {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(16.dp),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                LoadingIndicator(
//                                    color = RedditOrange
//                                )
//                            }
//                        }
//                    }
                } else {
                    when (postsState) {
                        is PostsState.Error -> {
                            val errorState = postsState as PostsState.Error
                            item {
                                ErrorMessage(
                                    error = errorState.message,
                                    onRetry = {
                                        TODO("error on retry")
//                                        viewModel.loadPosts(LoadType.MORE)
                                    },
                                    onDismiss = {
                                        TODO("error on dismiss")
//                                        viewModel.clearError()
                                    }
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
}

@Preview(
    showBackground = true
)
@Composable
private fun PreviewSubredditPostsSection() {
    SubredditPostsSection(
        isRefreshing = false,
        onSortClick = {},
        onRefresh = {},
        postsState = PostsState.Success.mock,
        blurNsfw = false,
        currentSort = SortType.HOT,
        isPersonalized = true,
        onSortChanged = {},
        onVote = { id, direction -> },
    )
}