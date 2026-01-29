package com.hamburghini.cosmos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.core.util.PhotoViewerUtils
import com.hamburghini.cosmos.core.util.PostType
import com.hamburghini.cosmos.core.util.PostUtils

@Composable
fun PostCard(
    post: Post,
    blurNsfw: Boolean,
    modifier: Modifier = Modifier,
    onPostClick: (Post) -> Unit = {},
    onVote: (String, Int) -> Unit = { _, _ -> },
    onMenuClick: (Post) -> Unit = {},
    onSubredditClick: (Post) -> Unit = {},
    onAuthorClick: (Post) -> Unit = {},
    onVideoClick: () -> Unit = {}
) {
    var currentScore by remember(post.score) {
        mutableIntStateOf(post.score)
    }

    var voteState by remember(post.likes) {
        mutableStateOf(post.likes)  // null = no vote, true = upvoted, false = downvoted
    }

    val postType = PostUtils.getPostType(post)
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { onPostClick(post) },
                onLongClick = { onMenuClick(post) }
            )
            .drawWithContent {
                drawContent()
                if (post.saved) {

                    val triangleSize = 24.dp.toPx()
                    val path = Path().apply {
                        moveTo(size.width, size.height)
                        lineTo(size.width - triangleSize, size.height)
                        lineTo(size.width, size.height - triangleSize)
                        close()
                    }

                    drawPath(path, Color(0, 200, 0))
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with subreddit and author info
            PostHeader(
                post = post,
                onSubredditClick = onSubredditClick,
                onAuthorClick = onAuthorClick
            )

            // Title
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Flair if available
            PostUtils.getFlairText(post)?.let { flair ->
                Text(
                    text = flair,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Content based on post type
            when (postType) {
                PostType.IMAGE -> PostGalleryContent(
                    post = post,
                    blurNsfw = blurNsfw,
                    onGalleryClick = {
                        // Launch photo viewer for single image
                        val redditImages = PhotoViewerUtils.extractImageUrls(post)
                        if (redditImages.isNotEmpty()) {
                            PhotoViewerUtils.launchPhotoViewer(
                                context = context,
                                redditImage = redditImages,
                                postName = post.name,
                                initialPage = 0
                            )
                        }
                    }
                )
                PostType.VIDEO -> PostVideoContent(
                    post = post,
                    onVideoClick = onVideoClick
                )
                PostType.TEXT -> PostTextContent(post)
                PostType.LINK -> PostLinkContent(post)
                PostType.GALLERY -> PostGalleryContent(
                    post = post,
                    blurNsfw = blurNsfw,
                    onGalleryClick = { index ->
                        // Launch photo viewer for gallery
                        val imageUrls = PhotoViewerUtils.extractImageUrls(post)
                        if (imageUrls.isNotEmpty()) {
                            PhotoViewerUtils.launchPhotoViewer(
                                context = context,
                                redditImage = imageUrls,
                                postName = post.name,
                                initialPage = index
                            )
                        }
                    }
                )
                PostType.UNKNOWN -> Unit
            }

            // Footer with voting, comments, share
            PostFooter(
                post = post,
                currentScore = currentScore,
                voteState = voteState,
                onUpvote = {
                    val newVote = if (voteState == true) 0 else 1
                    val scoreDiff = when (voteState) {
                        true if newVote == 0 -> -1 // Remove upvote
                        false -> 2 // Change from downvote to upvote
                        null -> 1 // Add upvote
                        else -> 0
                    }
                    currentScore += scoreDiff
                    voteState = if (newVote == 0) null else true
                    onVote(post.name, newVote)
                },
                onDownvote = {
                    val newVote = if (voteState == false) 0 else -1
                    val scoreDiff = when (voteState) {
                        false if newVote == 0 -> 1 // Remove downvote
                        true -> -2 // Change from upvote to downvote
                        null -> -1 // Add downvote
                        else -> 0
                    }
                    currentScore += scoreDiff
                    voteState = if (newVote == 0) null else false
                    onVote(post.name, newVote)
                },
                onMenuClick = onMenuClick
            )
        }
    }
}

@Preview
@Composable
fun PreviewPostCard() {
    PostCard(
        post = Post.mock.copy(likes = true),
        blurNsfw = true
    )
}