package com.hamburghini.cosmos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.hamburghini.cosmos.R
import com.hamburghini.cosmos.model.Image
import com.hamburghini.cosmos.ui.theme.RedditOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedditTopAppBar(
    title: String,
    showLogo: Boolean = false,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = navigationIcon,
        title = {
            if (showLogo) {
                // Show Reddit logo on home screen
                Image(
                    painter = painterResource(id = R.drawable.ic_reddit_logo_wordmark),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.height(24.dp),
                    contentScale = ContentScale.FillHeight,
                )
            } else {
                // Show text title on other screens
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = RedditOrange,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "showLogo = true"
)
@Composable
fun PreviewTopAppBarWithRedditLogo() {
    RedditTopAppBar(
        title = "Title",
        showLogo = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "showLogo = false"
)
@Composable
fun PreviewTopAppBarTextTitle() {
    RedditTopAppBar(
        title = "Title",
        showLogo = false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "navigationIcon"
)
@Composable
fun PreviewTopAppBar() {
    RedditTopAppBar(
        title = "Title",
        navigationIcon = {
            IconButton(
                onClick = {}
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                )
            }
        }
    )
}