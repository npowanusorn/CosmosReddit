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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.hamburghini.cosmos.R
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.data.model.SubredditAboutData
import com.hamburghini.cosmos.data.repository.LoadType
import com.hamburghini.cosmos.data.repository.PostsState
import com.hamburghini.cosmos.data.repository.SortType
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubredditDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: SubredditDetailViewModel,
    onScrollProgressChanged: (Float) -> Unit = {}
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
        onScrollProgressChanged = onScrollProgressChanged,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubredditDetailContent(
    subreddit: SubredditAboutData,
    postsState: PostsState,
    blurNsfw: Boolean,
    onRefresh: () -> Unit,
    onScrollProgressChanged: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val selectedTab = remember { mutableIntStateOf(0) }
    val tabs = SubredditDetailTabs.entries
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    // Calculate header collapse progress
    val headerHeight = 250.dp
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
                CollapsibleHeader(
                    subreddit = subreddit,
                    collapseProgress = collapseProgress.value,
                    modifier = Modifier.height(headerHeight)
                )
            }

            // Tab Row
            item {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab.intValue,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Color(0xFFFF4500)
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab.intValue == index,
                            onClick = { selectedTab.intValue = index },
                            text = { Text(tab.title) }
                        )
                    }
                }
            }

            // Tab Content
            item {
                when (selectedTab.intValue) {
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
private fun CollapsibleHeader(
    subreddit: SubredditAboutData,
    collapseProgress: Float,
    modifier: Modifier = Modifier
) {
    val alpha = 1f - collapseProgress
    val scale = max(0.8f, 1f - (collapseProgress * 0.2f))

    Box(modifier = modifier.fillMaxWidth()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .alpha(alpha)
        )

        // Gradient Overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(alpha)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .alpha(alpha)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size((64 * scale).dp)
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
                    color = Color(0xFF4CAF50)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "online",
                    color = Color.White.copy(alpha = 0.8f)
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