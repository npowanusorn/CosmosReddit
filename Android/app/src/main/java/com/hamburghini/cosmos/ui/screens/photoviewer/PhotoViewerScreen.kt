package com.hamburghini.cosmos.ui.screens.photoviewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.hamburghini.cosmos.core.util.Logger
import com.hamburghini.cosmos.core.util.PostUtils
import com.hamburghini.cosmos.core.util.RedditImage
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.ui.components.MediaScreenBottomBar
import com.hamburghini.cosmos.ui.components.PostMenuBottomSheet
import com.hamburghini.cosmos.ui.components.ZoomableImage
import kotlinx.coroutines.launch

/**
 * Photo viewer screen with swipeable gallery and zoomable images
 *
 * @param redditImages List of image URLs to display
 * @param initialPage Starting page index
 * @param onBackClick Callback when back button is pressed
 */
@Composable
fun PhotoViewerScreen(
    redditImages: List<RedditImage>,
    postId: String,
    onBackClick: () -> Unit,
    initialPage: Int = 0,
    viewModel: PhotoViewerViewModel = hiltViewModel()
) {
    val post by viewModel.postFlow(postId).collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showPostMenu by remember { mutableStateOf(false) }

    when (post) {
        null -> {
            TODO("null post")
        }
        else -> {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                PhotoViewerContent(
                    post = post!!,
                    redditImages = redditImages,
                    initialPage = initialPage,
                    onBackClick = onBackClick,
                    onMenuClick = {
                        showPostMenu = true
                    },
                    onUpvoteClick = {
                        val newVote = if (post!!.likes == true) 0 else 1
                        viewModel.voteOnPost(postId, newVote)
                    },
                    onDownvoteClick = {
                        val newVote = if (post!!.likes == false) 0 else -1
                        viewModel.voteOnPost(postId, newVote)
                    },
                    onShareClick = {
                        PostUtils.sharePost(context, post!!)
                    }
                )

                if (showPostMenu) {
                    PostMenuBottomSheet(
                        post = post!!,
                        isLoggedIn = viewModel.isLoggedIn(),
                        onDismissRequest = {
                            showPostMenu = false
                        },
                        onSaveClick = {
                            post?.let {
                                val newSaveState = !it.saved
                                viewModel.savePost(it.name, newSaveState)

//                            // Update selected post immediately for UI feedback
//                            selectedPost = post.copy(saved = newSaveState)

                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (newSaveState) "Post saved" else "Post unsaved"
                                    )
                                }
                            }
//                                val newSaveState = !post.saved
//                                viewModel.savePost(post.name, newSaveState)
//
//                                coroutineScope.launch {
//                                    snackbarHostState.showSnackbar(
//                                        if (newSaveState) "Post saved" else "Post unsaved"
//                                    )
//                                }
                        },
                        onHideClick = {
                            post?.let {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (it.hidden == true) "TODO: Post unhidden" else "TODO: Post hidden"
                                    )
                                }
                            }
                        },
                        onReportClick = {
                            post?.let {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Report functionality coming soon")
                                }
                            }
                        },
                        onShareClick = {
                            post?.let {
                                PostUtils.sharePost(context, it)
                            }
                        },
                        onCopyLinkClick = {
                            post?.let {
                                PostUtils.copyPostLink(context, it)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Link copied to clipboard")
                                }
                            }
                        },
                        onBlockUserClick = {
                            post?.let {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Block user functionality coming soon")
                                }
                            }
                        },
                        onViewProfileClick = {
                            post?.let {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Navigate to u/${it.author}")
                                }
                            }
                        },
                        onViewSubredditClick = {
                            post?.let {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Navigate to ${it.subreddit_name_prefixed}")
                                }
                            }
                        }
                    )
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                )
            }
        }
    }
}

@Composable
fun PhotoViewerContent(
    post: Post,
    redditImages: List<RedditImage>,
    initialPage: Int,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onUpvoteClick: () -> Unit,
    onDownvoteClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    var showUI by remember { mutableStateOf(true) }

    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, redditImages.size - 1),
        pageCount = { redditImages.size }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Image pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            ZoomableImage(
                redditImage = redditImages[page],
                onTap = { showUI = !showUI },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top bar with back button and page indicator
        AnimatedVisibility(
            visible = showUI,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            PhotoViewerTopBar(
                currentPage = pagerState.currentPage + 1,
                totalPages = redditImages.size,
                onBackClick = onBackClick,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(8.dp)
            )
        }

        AnimatedVisibility(
            visible = showUI,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            MediaScreenBottomBar(
                score = post.score,
                likes = post.likes,
                numComments = post.num_comments,
                onMenuClick = onMenuClick,
                onUpvoteClick = onUpvoteClick,
                onDownvoteClick = onDownvoteClick,
                onShareClick = onShareClick,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun PhotoViewerTopBar(
    currentPage: Int,
    totalPages: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // Page indicator
        if (totalPages > 1) {
            Text(
                text = "$currentPage / $totalPages",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
