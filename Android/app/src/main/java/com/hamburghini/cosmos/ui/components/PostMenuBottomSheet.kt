package com.hamburghini.cosmos.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hamburghini.cosmos.data.model.Post
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostMenuBottomSheet(
    post: Post,
    isLoggedIn: Boolean,
    onDismissRequest: () -> Unit,
    onSaveClick: () -> Unit,
    onHideClick: () -> Unit,
    onReportClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyLinkClick: () -> Unit,
    onBlockUserClick: () -> Unit,
    onViewProfileClick: () -> Unit,
    onViewSubredditClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        PostMenuContent(
            post = post,
            isLoggedIn = isLoggedIn,
            onSaveClick = {
                scope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
                onSaveClick()
            },
            onHideClick = {
                scope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
                onHideClick()
            },
            onReportClick = {
                scope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
                onReportClick()
            },
            onShareClick = {
                scope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
                onShareClick()
            },
            onCopyLinkClick = {
                scope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
                onCopyLinkClick()
            },
            onBlockUserClick = {
                scope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
                onBlockUserClick()
            },
            onViewProfileClick = {
                scope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
                onViewProfileClick()
            },
            onViewSubredditClick = {
                scope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
                onViewSubredditClick()
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PostMenuContent(
    post: Post,
    isLoggedIn: Boolean,
    onSaveClick: () -> Unit,
    onHideClick: () -> Unit,
    onReportClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyLinkClick: () -> Unit,
    onBlockUserClick: () -> Unit,
    onViewProfileClick: () -> Unit,
    onViewSubredditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Post Options",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        // Main Actions (always visible)
        MenuItemSection(title = "Actions") {
            MenuItem(
                icon = Icons.Default.Share,
                title = "Share",
                subtitle = "Share this post",
                onClick = onShareClick
            )

            MenuItem(
                icon = Icons.Default.ContentCopy,
                title = "Copy Link",
                subtitle = "Copy post URL to clipboard",
                onClick = onCopyLinkClick
            )

            if (isLoggedIn) {
                MenuItem(
                    icon = if (post.saved) Icons.Default.Bookmarks else Icons.Default.BookmarkBorder,
                    title = if (post.saved) "Unsave" else "Save",
                    subtitle = if (post.saved) "Remove from saved posts" else "Save for later",
                    onClick = onSaveClick
                )

                MenuItem(
                    icon = if (post.hidden == true) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    title = if (post.hidden == true) "Unhide" else "Hide",
                    subtitle = if (post.hidden == true) "Show this post" else "Hide from your feed",
                    onClick = onHideClick
                )
            }
        }

//        // User Actions (only when logged in)
//        if (isLoggedIn) {
//            HorizontalDivider(
//                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
//            )
//
//            MenuItemSection(title = "Your Actions") {
//                MenuItem(
//                    icon = if (post.saved) Icons.Default.Bookmarks else Icons.Default.BookmarkBorder,
//                    title = if (post.saved) "Unsave" else "Save",
//                    subtitle = if (post.saved) "Remove from saved posts" else "Save for later",
//                    onClick = onSaveClick
//                )
//
//                MenuItem(
//                    icon = if (post.hidden == true) Icons.Default.Visibility else Icons.Default.VisibilityOff,
//                    title = if (post.hidden == true) "Unhide" else "Hide",
//                    subtitle = if (post.hidden == true) "Show this post" else "Hide from your feed",
//                    onClick = onHideClick
//                )
//            }
//        }

        // Navigation
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        MenuItemSection(title = "Navigate") {
            MenuItem(
                icon = Icons.Default.Info,
                title = "View Profile",
                subtitle = "u/${post.author}",
                onClick = onViewProfileClick
            )

            MenuItem(
                icon = Icons.Default.Info,
                title = "View Subreddit",
                subtitle = post.subreddit_name_prefixed,
                onClick = onViewSubredditClick
            )
        }

        // Moderation Actions
        if (isLoggedIn) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            MenuItemSection(title = "Moderation") {
                MenuItem(
                    icon = Icons.Default.Flag,
                    title = "Report",
                    subtitle = "Report this post",
                    onClick = onReportClick,
                    isDestructive = true
                )

                MenuItem(
                    icon = Icons.Default.Block,
                    title = "Block User",
                    subtitle = "Block u/${post.author}",
                    onClick = onBlockUserClick,
                    isDestructive = true
                )
            }
        }
    }
}

@Composable
private fun MenuItemSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        content()
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isDestructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}