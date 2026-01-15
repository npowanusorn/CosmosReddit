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
import com.hamburghini.cosmos.model.Post
import com.hamburghini.cosmos.ui.theme.DownvoteColor
import com.hamburghini.cosmos.ui.theme.NeutralColor
import com.hamburghini.cosmos.ui.theme.UpvoteColor
import com.hamburghini.cosmos.core.util.Logger
import com.hamburghini.cosmos.core.util.PhotoViewerUtils
import com.hamburghini.cosmos.core.util.PostType
import com.hamburghini.cosmos.core.util.PostUtils

@Composable
fun PostDetailScreen(
    viewModel: PostDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val post by viewModel.post.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        post?.let { currentPost ->
            PostDetailContent(currentPost)
        }
    }
}

@Composable
private fun PostDetailContent(
    currentPost: Post
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showPostMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Post Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PostDetailCard(
                        post = currentPost,
                        currentScore = currentPost.score,
                        voteState = currentPost.likes,
                        onUpvote = {
                            val newVote = if (currentPost.likes == true) 0 else 1
                            val scoreDiff = when (currentPost.likes) {
                                true if newVote == 0 -> -1
                                false -> 2
                                null -> 1
                                else -> 0
                            }
                            currentPost.score += scoreDiff
                            currentPost.likes = if (newVote == 0) null else true
                            Logger.i("TODO handle vote")
                        },
                        onDownvote = {
                            val newVote = if (currentPost.likes == false) 0 else -1
                            val scoreDiff = when (currentPost.likes) {
                                false if newVote == 0 -> 1
                                true -> -2
                                null -> -1
                                else -> 0
                            }
                            currentPost.score += scoreDiff
                            currentPost.likes = if (newVote == 0) null else false
                            Logger.i("TODO handle vote")
//                            homeViewModel.voteOnPost(post.name, newVote)
                        },
                        context = context
                    )
                }

                // Comments section placeholder
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Comments",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Comments Coming Soon",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${PostUtils.formatCommentCount(currentPost.num_comments)} comments",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Post Menu Bottom Sheet
//        if (showPostMenu) {
//            PostMenuBottomSheet(
//                post = currentPost,
//                isLoggedIn = homeViewModel.isLoggedIn(),
//                onDismissRequest = { showPostMenu = false },
//                onSaveClick = {
//                    val newSaveState = !currentPost.saved
//                    homeViewModel.savePost(currentPost.name, newSaveState)
//                    currentPost = currentPost.copy(saved = newSaveState)
//                    coroutineScope.launch {
//                        snackbarHostState.showSnackbar(
//                            if (newSaveState) "Post saved" else "Post unsaved"
//                        )
//                    }
//                },
//                onHideClick = {
//                    coroutineScope.launch {
//                        snackbarHostState.showSnackbar("Hide functionality coming soon")
//                    }
//                },
//                onReportClick = {
//                    coroutineScope.launch {
//                        snackbarHostState.showSnackbar("Report functionality coming soon")
//                    }
//                },
//                onShareClick = {
//                    sharePost(context, currentPost)
//                },
//                onCopyLinkClick = {
//                    copyPostLink(context, currentPost)
//                    coroutineScope.launch {
//                        snackbarHostState.showSnackbar("Link copied to clipboard")
//                    }
//                },
//                onBlockUserClick = {
//                    coroutineScope.launch {
//                        snackbarHostState.showSnackbar("Block user functionality coming soon")
//                    }
//                },
//                onViewProfileClick = {
//                    coroutineScope.launch {
//                        snackbarHostState.showSnackbar("Navigate to u/${currentPost.author}")
//                    }
//                },
//                onViewSubredditClick = {
//                    coroutineScope.launch {
//                        snackbarHostState.showSnackbar("Navigate to ${currentPost.subreddit_name_prefixed}")
//                    }
//                }
//            )
//        }
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            PostHeader(post = post)

            // Title
            Text(
                text = post.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
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
                PostType.IMAGE -> PostImageContent(
                    post = post,
                    onImageClick = {
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
                onDownvote = onDownvote
            )
        }
    }
}

@Composable
private fun PostHeader(post: Post) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = post.subreddit_name_prefixed,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "•",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "u/${post.author}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = PostUtils.formatTimeAgo(post.created_utc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PostImageContent(
    post: Post,
    onImageClick: () -> Unit
) {
    PostUtils.getImageUrl(post)?.let { imageUrl ->
        AsyncImage(
            model = imageUrl,
            contentDescription = "Post image",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black),
            contentScale = ContentScale.Fit,
            error = painterResource(R.drawable.ic_launcher_background)
        )
    }
}

@Composable
private fun PostVideoContent(post: Post) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        PostUtils.getThumbnailUrl(post)?.let { thumbnailUrl ->
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "Video thumbnail",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }

        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play video",
            tint = Color.White,
            modifier = Modifier
                .size(64.dp)
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    RoundedCornerShape(32.dp)
                )
                .padding(16.dp)
        )
    }
}

@Composable
private fun PostTextContent(post: Post) {
    PostUtils.cleanSelfText(post.selftext)?.let { text ->
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PostLinkContent(post: Post) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Link,
            contentDescription = "External link",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = post.url,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PostGalleryContent(
    post: Post,
    onGalleryClick: () -> Unit
) {
    val imageCount = post.gallery_data?.items?.size ?: 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = "Gallery post",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Gallery ($imageCount images)",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PostFooter(
    post: Post,
    currentScore: Int,
    voteState: Boolean?,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onUpvote,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Upvote",
                    tint = when (voteState) {
                        true -> UpvoteColor
                        else -> NeutralColor
                    },
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = PostUtils.formatScore(currentScore),
                style = MaterialTheme.typography.titleMedium,
                color = when (voteState) {
                    true -> UpvoteColor
                    false -> DownvoteColor
                    null -> NeutralColor
                },
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = onDownvote,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Downvote",
                    tint = when (voteState) {
                        false -> DownvoteColor
                        else -> NeutralColor
                    },
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = "Comments",
                tint = NeutralColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = PostUtils.formatCommentCount(post.num_comments),
                style = MaterialTheme.typography.bodyLarge,
                color = NeutralColor,
                fontWeight = FontWeight.Medium
            )
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

@Preview(
    name = "PostDetailScreen",
    device = Devices.PHONE
)
@Composable
fun PreviewPostDetailScreen() {
    PostDetailContent(
        currentPost = Post.mock
    )
}