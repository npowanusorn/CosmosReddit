package com.hamburghini.cosmos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hamburghini.cosmos.ui.components.ZoomableImage

/**
 * Photo viewer screen with swipeable gallery and zoomable images
 *
 * @param imageUrls List of image URLs to display
 * @param initialPage Starting page index
 * @param onBackClick Callback when back button is pressed
 */
@Composable
fun PhotoViewerScreen(
    imageUrls: List<String>,
    initialPage: Int = 0,
    onBackClick: () -> Unit
) {
    var showUI by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, imageUrls.size - 1),
        pageCount = { imageUrls.size }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Image pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            ZoomableImage(
                imageUrl = imageUrls[page],
                onTap = { showUI = !showUI },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top bar with back button and page indicator
        AnimatedVisibility(
            visible = showUI,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            PhotoViewerTopBar(
                currentPage = pagerState.currentPage + 1,
                totalPages = imageUrls.size,
                onBackClick = onBackClick,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun PhotoViewerTopBar(
    currentPage: Int,
    totalPages: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // Page indicator
        if (totalPages > 1) {
            Text(
                text = "$currentPage / $totalPages",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
