package com.hamburghini.cosmos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hamburghini.cosmos.core.util.CommentTreeUtils
import com.hamburghini.cosmos.core.util.PostUtils
import com.hamburghini.cosmos.data.model.Comment
import com.hamburghini.cosmos.ui.theme.DownvoteColor
import com.hamburghini.cosmos.ui.theme.NeutralColor
import com.hamburghini.cosmos.ui.theme.RedditOrange
import com.hamburghini.cosmos.ui.theme.UpvoteColor

/**
 * Maximum depth for comment nesting visualization
 * Beyond this depth, comments won't indent further to save space
 */
private const val MAX_VISUAL_DEPTH = 8

/**
 * Width of each indentation level in dp
 */
private const val INDENT_WIDTH = 12

/**
 * A composable that displays a single comment with proper threading support
 */
@Composable
fun CommentItem(
    comment: Comment,
    depth: Int = 0,
    onVote: (String, Int) -> Unit = { _, _ -> },
    onReply: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentScore by remember { mutableIntStateOf(comment.score) }
    var voteState by remember { mutableStateOf<Boolean?>(null) }
    var isCollapsed by remember { mutableStateOf(false) }

    val visualDepth = depth.coerceAtMost(MAX_VISUAL_DEPTH)
    val indentSize = (visualDepth * INDENT_WIDTH).dp

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = indentSize)
        ) {
            // Depth indicator line
            if (depth > 0) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(if (isCollapsed) 48.dp else 999.dp)
                        .background(
                            color = getDepthColor(depth),
                            shape = RoundedCornerShape(1.dp)
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))
            }

            // Comment content
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Comment header
                    CommentHeader(
                        author = comment.author,
                        timeAgo = PostUtils.formatTimeAgo(comment.created_utc),
                        isCollapsed = isCollapsed,
                        onToggleCollapse = { isCollapsed = !isCollapsed }
                    )

                    // Comment body (hidden when collapsed)
                    if (!isCollapsed) {
                        Text(
                            text = comment.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Comment actions
                        CommentActions(
                            score = currentScore,
                            voteState = voteState,
                            onUpvote = {
                                val newVote = if (voteState == true) 0 else 1
                                val scoreDiff = when (voteState) {
                                    true -> if (newVote == 0) -1 else 0
                                    false -> 2
                                    null -> 1
                                }
                                currentScore += scoreDiff
                                voteState = if (newVote == 0) null else true
                                onVote(comment.name, newVote)
                            },
                            onDownvote = {
                                val newVote = if (voteState == false) 0 else -1
                                val scoreDiff = when (voteState) {
                                    false -> if (newVote == 0) 1 else 0
                                    true -> -2
                                    null -> -1
                                }
                                currentScore += scoreDiff
                                voteState = if (newVote == 0) null else false
                                onVote(comment.name, newVote)
                            },
                            onReply = { onReply(comment.id) }
                        )
                    }
                }
            }
        }

        // Render child comments (replies) if not collapsed
        if (!isCollapsed && comment.replies != null) {
            val childComments = parseCommentReplies(comment.replies)
            childComments.forEach { childComment ->
                Spacer(modifier = Modifier.height(4.dp))
                CommentItem(
                    comment = childComment,
                    depth = depth + 1,
                    onVote = onVote,
                    onReply = onReply
                )
            }
        }
    }
}

@Composable
private fun CommentHeader(
    author: String,
    timeAgo: String,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Author avatar placeholder
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(RedditOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = author.take(1).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "u/$author",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "•",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = timeAgo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Collapse/expand button
        IconButton(
            onClick = onToggleCollapse,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = if (isCollapsed) "Expand" else "Collapse",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CommentActions(
    score: Int,
    voteState: Boolean?,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit,
    onReply: () -> Unit
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
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Upvote",
                    tint = when (voteState) {
                        true -> UpvoteColor
                        else -> NeutralColor
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = PostUtils.formatScore(score),
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
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Downvote",
                    tint = when (voteState) {
                        false -> DownvoteColor
                        else -> NeutralColor
                    },
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onReply,
                modifier = Modifier.height(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Reply,
                    contentDescription = "Reply",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Reply",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            IconButton(
                onClick = { /* TODO: More options */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = NeutralColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Get color for depth indicator line based on nesting level
 */
@Composable
private fun getDepthColor(depth: Int): androidx.compose.ui.graphics.Color {
    val colors = listOf(
        RedditOrange,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )

    return colors[depth % colors.size].copy(alpha = 0.6f)
}

/**
 * Parse comment replies from the Reddit API response
 * The replies field can be empty string, null, or a listing object
 */
private fun parseCommentReplies(replies: Any?): List<Comment> {
    return CommentTreeUtils.parseCommentReplies(replies)
}