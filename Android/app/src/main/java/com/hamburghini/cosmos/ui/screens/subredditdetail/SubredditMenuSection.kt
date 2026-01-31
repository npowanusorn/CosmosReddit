package com.hamburghini.cosmos.ui.screens.subredditdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.Rule
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SubredditMenuSection(
    modifier: Modifier = Modifier,
    showRulesSection: Boolean,
    onRulesClick: () -> Unit,
    onModeratorsClick: () -> Unit,
    onContactModsClick: () -> Unit,
    onWikiClick: () -> Unit,
    onShareClick: () -> Unit,
    onViewInBrowserClick: () -> Unit,
    onReportClick: () -> Unit,
    rules: List<String> = emptyList()
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        // Menu Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.surfaceVariant
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column {
                    MenuItemRow(
                        icon = Icons.AutoMirrored.Outlined.Rule,
                        title = "Rules",
                        onClick = onRulesClick
                    )

                    Divider()

                    MenuItemRow(
                        icon = Icons.Outlined.Security,
                        title = "Moderators",
                        onClick = onModeratorsClick
                    )

                    Divider()

                    MenuItemRow(
                        icon = Icons.Outlined.Email,
                        title = "Contact Moderators",
                        onClick = onContactModsClick
                    )

                    Divider()

                    MenuItemRow(
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        title = "Wiki",
                        trailingIcon = Icons.AutoMirrored.Outlined.OpenInNew,
                        onClick = onWikiClick
                    )

                    Divider()

                    MenuItemRow(
                        icon = Icons.Outlined.Share,
                        title = "Share Community",
                        trailingIcon = null,
                        onClick = onShareClick
                    )

                    Divider()

                    MenuItemRow(
                        icon = Icons.Outlined.Public,
                        title = "View in Browser",
                        trailingIcon = Icons.AutoMirrored.Outlined.OpenInNew,
                        onClick = onViewInBrowserClick
                    )

                    Divider()

                    MenuItemRow(
                        icon = Icons.Outlined.Flag,
                        title = "Report",
                        onClick = onReportClick
                    )
                }
            }
        }

        // Rules Section
        if (showRulesSection) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Community Rules",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    rules.forEachIndexed { index, rule ->
                        RuleItem(
                            index = index + 1,
                            text = rule
                        )
                    }
                }
            }
        }
    }
}

// Content version for use within LazyColumn items
@Composable
fun SubredditMenuSectionContent(
    showRulesSection: Boolean = false,
    onRulesClick: () -> Unit = {},
    onModeratorsClick: () -> Unit = {},
    onContactModsClick: () -> Unit = {},
    onWikiClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onViewInBrowserClick: () -> Unit = {},
    onReportClick: () -> Unit = {},
    rules: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Menu Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.surfaceVariant
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                MenuItemRow(
                    icon = Icons.AutoMirrored.Outlined.Rule,
                    title = "Rules",
                    onClick = onRulesClick
                )

                Divider()

                MenuItemRow(
                    icon = Icons.Outlined.Security,
                    title = "Moderators",
                    onClick = onModeratorsClick
                )

                Divider()

                MenuItemRow(
                    icon = Icons.Outlined.Email,
                    title = "Contact Moderators",
                    onClick = onContactModsClick
                )

                Divider()

                MenuItemRow(
                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                    title = "Wiki",
                    trailingIcon = Icons.AutoMirrored.Outlined.OpenInNew,
                    onClick = onWikiClick
                )

                Divider()

                MenuItemRow(
                    icon = Icons.Outlined.Share,
                    title = "Share Community",
                    trailingIcon = null,
                    onClick = onShareClick
                )

                Divider()

                MenuItemRow(
                    icon = Icons.Outlined.Public,
                    title = "View in Browser",
                    trailingIcon = Icons.AutoMirrored.Outlined.OpenInNew,
                    onClick = onViewInBrowserClick
                )

                Divider()

                MenuItemRow(
                    icon = Icons.Outlined.Flag,
                    title = "Report",
                    onClick = onReportClick
                )
            }
        }

        // Rules Section
        if (showRulesSection) {
            Spacer(modifier = Modifier.padding(top = 16.dp))

            Text(
                text = "Community Rules",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            rules.forEachIndexed { index, rule ->
                RuleItem(
                    index = index + 1,
                    text = rule
                )
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    trailingIcon: ImageVector? = Icons.Outlined.ChevronRight
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        trailingIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.surfaceVariant,
        thickness = 1.dp
    )
}

@Composable
private fun RuleItem(
    index: Int,
    text: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "$index. $text",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSubredditMenuSection() {
    SubredditMenuSection(
        showRulesSection = true,
        onRulesClick = {},
        onModeratorsClick = {},
        onContactModsClick = {},
        onWikiClick = {},
        onShareClick = {},
        onViewInBrowserClick = {},
        onReportClick = {}
    )
}