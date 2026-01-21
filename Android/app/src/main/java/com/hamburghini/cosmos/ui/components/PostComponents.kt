package com.hamburghini.cosmos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.hamburghini.cosmos.core.util.PhotoViewerUtils
import com.hamburghini.cosmos.core.util.PostUtils
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.ui.theme.DownvoteColor
import com.hamburghini.cosmos.ui.theme.NeutralColor
import com.hamburghini.cosmos.ui.theme.UpvoteColor

@Composable
fun PostGalleryContent(
    post: Post,
    onGalleryClick: (Int) -> Unit
) {
    val redditImages = PhotoViewerUtils.extractImageUrls(post)

    var revealed by remember { mutableStateOf(false) }
    val isBlurred = (post.over_18 || post.spoiler == true) && !revealed

    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
    ) {

        ImagePreviewGrid(
            redditImages = redditImages,
            onClick = { index ->
                if (isBlurred) {
                    revealed = true
                } else {
                    onGalleryClick(index)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .blur(if (isBlurred) 32.dp else 0.dp)
        )

        if (isBlurred) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                // Overlay content (no background)
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (post.over_18) "NSFW" else "Spoiler",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    TextButton(onClick = { revealed = true }) {
                        Text("Tap to reveal", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PostHeader(
    post: Post,
    onSubredditClick: (Post) -> Unit = {},
    onAuthorClick: (Post) -> Unit = {}
) {
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
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onSubredditClick(post) }
                    .padding(vertical = 2.dp, horizontal = 4.dp)

            )
            Text(
                text = "•",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "u/${post.author}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onAuthorClick(post) }
                    .padding(vertical = 2.dp, horizontal = 4.dp)
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
fun PostVideoContent(
    post: Post,
    onVideoClick: () -> Unit = {}
) {
    PostUtils.getThumbnailUrl(post)?.let { pair ->
        val thumbnailUrl = pair.first
        val aspectRatio = pair.second

        Box(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(aspectRatio)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .clickable(onClick = { onVideoClick() }),
            contentAlignment = Alignment.Center
        ) {
            // Thumbnail
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "Video thumbnail",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillHeight
            )

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
}

@Composable
fun PostTextContent(post: Post) {
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
fun PostLinkContent(post: Post) {
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
fun PostFooter(
    post: Post,
    currentScore: Int,
    voteState: Boolean?,
    onUpvote: () -> Unit = {},
    onDownvote: () -> Unit = {},
    onMenuClick: ((Post) -> Unit)? = null
) {
    val targetColor = when (voteState) {
        true -> UpvoteColor.copy(alpha = 0.35f)
        false -> DownvoteColor.copy(alpha = 0.35f)
        null -> MaterialTheme.colorScheme.surface
    }

    val animatedBackground by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500),
        label = "VoteBackground"
    )

    val hapticFeedback = LocalHapticFeedback.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Voting controls
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = animatedBackground
            ),
            border = BorderStroke(
                width = if (voteState == null) 1.dp else 0.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        onUpvote()
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    },
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
                    onClick = {
                        onDownvote()
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    },
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

            if (onMenuClick != null) {
                IconButton(
                    onClick = { onMenuClick(post) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = NeutralColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewPostFooter() {
    PostFooter(
        post = Post.mock,
        currentScore = 100,
        voteState = null,
    )
}