package com.hamburghini.cosmos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.hamburghini.cosmos.R
import com.hamburghini.cosmos.model.Post
import com.hamburghini.cosmos.ui.theme.DownvoteColor
import com.hamburghini.cosmos.ui.theme.NeutralColor
import com.hamburghini.cosmos.ui.theme.UpvoteColor
import com.hamburghini.cosmos.util.PostType
import com.hamburghini.cosmos.util.PostUtils

@Composable
fun PostCard(
    post: Post,
    modifier: Modifier = Modifier,
    onPostClick: (Post) -> Unit = {},
    onVote: (String, Int) -> Unit = { _, _ -> }
) {
    var currentScore by remember { mutableIntStateOf(post.score) }
    var voteState by remember { mutableStateOf(post.likes) } // null = no vote, true = upvoted, false = downvoted

    val postType = PostUtils.getPostType(post)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPostClick(post) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with subreddit and author info
            PostHeader(post = post)

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
                PostType.IMAGE -> PostImageContent(post)
                PostType.VIDEO -> PostVideoContent(post)
                PostType.TEXT -> PostTextContent(post)
                PostType.LINK -> PostLinkContent(post)
                PostType.GALLERY -> PostGalleryContent(post)
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
                }
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
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "•",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "u/${post.author}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = PostUtils.formatTimeAgo(post.created_utc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PostImageContent(post: Post) {
    PostUtils.getImageUrl(post)?.let { imageUrl ->
        AsyncImage(
            model = imageUrl,
            contentDescription = "Post image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_launcher_background)
        )
    }
}

@Composable
private fun PostVideoContent(post: Post) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Thumbnail
        PostUtils.getThumbnailUrl(post)?.let { thumbnailUrl ->
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "Video thumbnail",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }

        // Play button overlay
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play video",
            tint = Color.White,
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    RoundedCornerShape(24.dp)
                )
                .padding(12.dp)
        )
    }
}

@Composable
private fun PostTextContent(post: Post) {
    PostUtils.cleanSelfText(post.selftext)?.let { text ->
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PostLinkContent(post: Post) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PostGalleryContent(post: Post) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = "Gallery post",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Gallery (${post.gallery_data?.items?.size ?: "Multiple"} images)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
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
        // Voting controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onUpvote,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Upvote",
                    tint = when (voteState) {
                        true -> UpvoteColor
                        else -> NeutralColor
                    }
                )
            }

            Text(
                text = PostUtils.formatScore(currentScore),
                style = MaterialTheme.typography.labelMedium,
                color = when (voteState) {
                    true -> UpvoteColor
                    false -> DownvoteColor
                    null -> NeutralColor
                },
                fontWeight = FontWeight.Medium
            )

            IconButton(
                onClick = onDownvote,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Downvote",
                    tint = when (voteState) {
                        false -> DownvoteColor
                        else -> NeutralColor
                    }
                )
            }
        }

        // Comments and share
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Comments",
                    tint = NeutralColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = PostUtils.formatCommentCount(post.num_comments),
                    style = MaterialTheme.typography.labelMedium,
                    color = NeutralColor
                )
            }

            IconButton(
                onClick = { /* TODO: Implement share */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = NeutralColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}