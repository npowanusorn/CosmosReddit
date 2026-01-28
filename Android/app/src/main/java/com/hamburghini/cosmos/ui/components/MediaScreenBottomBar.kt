package com.hamburghini.cosmos.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hamburghini.cosmos.core.util.PostUtils
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.ui.theme.DownvoteColor
import com.hamburghini.cosmos.ui.theme.NeutralColor
import com.hamburghini.cosmos.ui.theme.UpvoteColor

@Composable
fun MediaScreenBottomBar(
    score: Int,
    likes: Boolean?,
    numComments: Int,
    modifier: Modifier = Modifier,
    onUpvoteClick: () -> Unit = {},
    onDownvoteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onUpvoteClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Upvote",
                    tint = when (likes) {
                        true -> UpvoteColor
                        else -> LocalContentColor.current
                    }
                )
            }
            Text(PostUtils.formatScore(score))
            IconButton(onClick = onDownvoteClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Downvote",
                    tint = when (likes) {
                        false -> DownvoteColor
                        else -> LocalContentColor.current
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(PostUtils.formatScore(numComments))
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onShareClick) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.MoreHoriz, contentDescription = "More")
            }
        }
    }
}

@Preview
@Composable
fun PreviewMediaScreenBottomBar() {
    MediaScreenBottomBar(
        score = 123,
        likes = true,
        numComments = 234
    )
}