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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hamburghini.cosmos.R
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.data.model.SubredditAboutData
import com.hamburghini.cosmos.data.repository.LoadType
import com.hamburghini.cosmos.data.repository.PostsState
import com.hamburghini.cosmos.data.repository.SortType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubredditDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: SubredditDetailViewModel
) {

    val postsState by viewModel.postsState.collectAsState()
    val blurNsfw by viewModel.blurNsfw.collectAsStateWithLifecycle()

    SubredditDetailContent(
        subreddit = SubredditAboutData.mock,
        postsState = postsState,
        blurNsfw = blurNsfw,
        onRefresh = {
            viewModel.loadPosts(LoadType.REFRESH)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubredditDetailContent(
    subreddit: SubredditAboutData,
    postsState: PostsState,
    blurNsfw: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = SubredditDetailTabs.entries

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // --- Banner + Header ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
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
                                Color.Black.copy(alpha = 1f)
                            )
                        )
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
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color(0xFFFF4500) // reddit orange
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(tab.title) }
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
                SubredditPostsSection(
                    postsState = postsState,
                    isRefreshing = false,
                    onSortClick = {},
                    onRefresh = onRefresh,
                    blurNsfw = blurNsfw,
                    currentSort = SortType.HOT,
                    isPersonalized = true,
                    onSortChanged = {},
                    onVote = { postId, direction ->
                        
                    },
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
        subreddit = SubredditAboutData.mock,
        postsState = PostsState.Success(
            posts = mapOf(Pair("id", Post.mock)),
            sortType = SortType.HOT,
            currentAfter = null
        ),
        onRefresh = {},
        blurNsfw = false,
    )
}