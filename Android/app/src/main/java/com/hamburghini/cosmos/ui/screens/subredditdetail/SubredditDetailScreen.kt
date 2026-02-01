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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.hamburghini.cosmos.R
import com.hamburghini.cosmos.core.util.Logger
import com.hamburghini.cosmos.core.util.PostUtils
import com.hamburghini.cosmos.core.util.SubredditUtils
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.data.model.SubredditAboutData
import com.hamburghini.cosmos.data.repository.LoadType
import com.hamburghini.cosmos.data.repository.PostsState
import com.hamburghini.cosmos.data.repository.SortType
import com.hamburghini.cosmos.ui.theme.RedditOrange

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SubredditDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: SubredditDetailViewModel,
    onScrollProgressChanged: (Float) -> Unit = {}
) {
    val postsState by viewModel.postsState.collectAsState()
    val subredditAbout by viewModel.subredditAbout.collectAsState()
    val blurNsfw by viewModel.blurNsfw.collectAsStateWithLifecycle()

    if (subredditAbout != null) {
        SubredditDetailContent(
            subreddit = subredditAbout!!,
            postsState = postsState,
            blurNsfw = blurNsfw,
            onRefresh = {
                viewModel.loadPosts(LoadType.REFRESH)
            },
            onScrollProgressChanged = onScrollProgressChanged,
            modifier = modifier
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            LoadingIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubredditDetailContent(
    subreddit: SubredditAboutData,
    postsState: PostsState,
    blurNsfw: Boolean,
    onRefresh: () -> Unit,
    onScrollProgressChanged: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = SubredditDetailTabs.entries
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    // Calculate header collapse progress
    val headerHeight = 350.dp
    val headerHeightPx = with(density) { headerHeight.toPx() }

    val collapseProgress = remember {
        derivedStateOf {
            val firstVisibleItemIndex = listState.firstVisibleItemIndex
            val firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset

            when {
                // If we're still on the header item (index 0)
                firstVisibleItemIndex == 0 -> {
                    // Progress from 0 to 1 as we scroll through the header
                    (firstVisibleItemScrollOffset / headerHeightPx).coerceIn(0f, 1f)
                }
                // If we've scrolled past the header completely
                firstVisibleItemIndex > 0 -> 1f
                // Default case
                else -> 0f
            }
        }
    }

    // Notify parent of scroll progress changes
    LaunchedEffect(collapseProgress.value) {
        onScrollProgressChanged(collapseProgress.value)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Banner
            item {
                SubredditHeader(
                    subreddit = subreddit,
                    selectedTab = selectedTab,
                    tabs = tabs.toList(),
                    onTabSelected = { newTab ->
                        selectedTab = newTab
                    },
                    modifier = Modifier.height(headerHeight)
                )
            }

            // Tab Content
            item {
                when (selectedTab) {
                    0 -> SubredditPostsSectionContent(
                        postsState = postsState,
                        blurNsfw = blurNsfw,
                        onRefresh = onRefresh,
                        onVote = { _, _ -> }
                    )
                    1 -> SubredditAboutSectionContent(subreddit = subreddit)
                    2 -> SubredditMenuSectionContent()
                }
            }
        }
    }
}

@Composable
private fun SubredditHeader(
    subreddit: SubredditAboutData,
    selectedTab: Int,
    tabs: List<SubredditDetailTabs>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabRowHeight = 48.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Background image
        AsyncImage(
            model = SubredditUtils.getBannerUrl(subreddit),
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
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Header content (kept away from tabs at bottom)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp)
                .padding(bottom = tabRowHeight + 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(16.dp))

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

            Spacer(Modifier.height(12.dp))

            Text(
                text = subreddit.publicDescription.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Group,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = PostUtils.formatScore(subreddit.subscribers),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = { Logger.d("join clicked") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedditOrange,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Join")
                }
            }
        }

        // Tabs inside the header at bottom
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            containerColor = Color.Transparent,
            contentColor = RedditOrange,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTab),
                    width = 32.dp,
                    color = RedditOrange
                )
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = tab.title,
                            color = if (selectedTab == index) RedditOrange else Color.White
                        )
                    }
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