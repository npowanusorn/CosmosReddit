package com.hamburghini.cosmos.ui.screens.subredditdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hamburghini.cosmos.R
import com.hamburghini.cosmos.data.model.SubredditAboutData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubredditDetailScreen(
    modifier: Modifier = Modifier
) {
//    SubredditDetailContent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubredditDetailContent(
    subreddit: SubredditAboutData,
    modifier: Modifier = Modifier,
    tabs: List<String> = listOf("Posts", "About", "Menu")
) {
    var selectedTab by remember { mutableIntStateOf(2) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // --- Banner + Header ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background), // replace
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            // Toolbar
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { /* back */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Subreddit content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground), // replace
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = subreddit.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = subreddit.displayNamePrefixed,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = subreddit.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row {
                    Text(
                        text = "${subreddit.accountsActive}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "members",
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(Modifier.width(24.dp))

                    Text(
                        text = "${subreddit.activeUserCount}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50) // online green
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "online",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // --- Tabs ---
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color(0xFFFF4500) // reddit orange
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // --- Pager content placeholder ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (selectedTab == 0) {
                Text(
                    text = "Selected tab: ${tabs[selectedTab]}",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else if (selectedTab == 1) {
                SubredditAboutSection(subreddit = subreddit)
            } else {
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
        }
    }
}

@Preview
@Composable
private fun PreviewSubredditDetailScreen() {
    SubredditDetailContent(
        subreddit = SubredditAboutData.mock
    )
}