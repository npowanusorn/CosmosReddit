package com.hamburghini.cosmos.ui.screens.postdetail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.hamburghini.cosmos.R
import com.hamburghini.cosmos.core.util.CommentTreeUtils
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.ui.theme.DownvoteColor
import com.hamburghini.cosmos.ui.theme.NeutralColor
import com.hamburghini.cosmos.ui.theme.UpvoteColor
import com.hamburghini.cosmos.core.util.Logger
import com.hamburghini.cosmos.core.util.PhotoViewerUtils
import com.hamburghini.cosmos.core.util.PostType
import com.hamburghini.cosmos.core.util.PostUtils
import com.hamburghini.cosmos.data.repository.CommentsState
import com.hamburghini.cosmos.ui.components.CommentItem
import com.hamburghini.cosmos.ui.components.PostFooter
import com.hamburghini.cosmos.ui.components.PostGalleryContent
import com.hamburghini.cosmos.ui.components.PostHeader
import com.hamburghini.cosmos.ui.components.PostLinkContent
import com.hamburghini.cosmos.ui.components.PostTextContent
import com.hamburghini.cosmos.ui.components.PostVideoContent

@Composable
fun PostDetailScreen(
    viewModel: PostDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val post by viewModel.post.collectAsState()
    val commentsState by viewModel.commentsState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        post?.let { currentPost ->
            PostDetailContent(
                currentPost = currentPost,
                commentsState = commentsState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PostDetailContent(
    currentPost: Post,
    commentsState: CommentsState
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Post content
            item {
                PostDetailCard(
                    post = currentPost,
                    currentScore = currentPost.score,
                    voteState = currentPost.likes,
                    onUpvote = { /* ... */ },
                    onDownvote = { /* ... */ },
                    context = context
                )
            }

            // Comments section
            when (commentsState) {
                is CommentsState.Error -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = "Error: ${commentsState.message}",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                is CommentsState.Loaded -> {
                    val flattenedComments = CommentTreeUtils.flattenCommentTree(commentsState.comments)

                    item {
                        Text(
                            text = "${flattenedComments.size} Comments",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(
                        items = flattenedComments,
                        key = { it.comment.id }
                    ) { commentWithDepth ->
                        CommentItem(
                            comment = commentWithDepth.comment,
                            depth = commentWithDepth.depth
                        )
                    }
                }
                CommentsState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    }
                }
                CommentsState.NotLoaded -> {
                    item {
                        Text("Comments not loaded")
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun PostDetailCard(
    post: Post,
    currentScore: Int,
    voteState: Boolean?,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit,
    context: Context
) {
    val postType = PostUtils.getPostType(post)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        PostHeader(
            post = post,
            onAuthorClick = {},
            onSubredditClick = {}
        )

        // Title
        Text(
            text = post.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Flair
        PostUtils.getFlairText(post)?.let { flair ->
            Text(
                text = flair,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Content
        when (postType) {
            PostType.IMAGE -> PostGalleryContent(
                post = post,
                onGalleryClick = {
                    val imageUrls = PhotoViewerUtils.extractImageUrls(post)
                    if (imageUrls.isNotEmpty()) {
                        PhotoViewerUtils.launchPhotoViewer(context, imageUrls, 0)
                    }
                }
            )
            PostType.VIDEO -> PostVideoContent(post)
            PostType.TEXT -> PostTextContent(post)
            PostType.LINK -> PostLinkContent(post)
            PostType.GALLERY -> PostGalleryContent(
                post = post,
                onGalleryClick = {
                    val imageUrls = PhotoViewerUtils.extractImageUrls(post)
                    if (imageUrls.isNotEmpty()) {
                        PhotoViewerUtils.launchPhotoViewer(context, imageUrls, 0)
                    }
                }
            )
            PostType.UNKNOWN -> Unit
        }

        // Voting and stats
        PostFooter(
            post = post,
            currentScore = currentScore,
            voteState = voteState,
            onUpvote = onUpvote,
            onDownvote = onDownvote,
            onMenuClick = null
        )
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

@Preview(
    name = "PostDetailScreen",
    device = Devices.PHONE
)
@Composable
fun PreviewPostDetailScreen() {
    PostDetailContent(
        currentPost = Post.mock.copy(likes = false),
        commentsState = CommentsState.Loading
    )
}