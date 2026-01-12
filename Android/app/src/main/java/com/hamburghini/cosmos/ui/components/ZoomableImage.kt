package com.hamburghini.cosmos.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.hamburghini.cosmos.ui.theme.RedditOrange
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

/**
 * Zoomable image component with pinch-to-zoom and double-tap gestures
 *
 * @param imageUrl URL of the image to display
 * @param onTap Callback when image is tapped
 * @param modifier Modifier to be applied
 */
@Composable
fun ZoomableImage(
    imageUrl: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val painter = rememberAsyncImagePainter(imageUrl)
    val zoomState = rememberZoomState(contentSize = painter.intrinsicSize)

    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        onSuccess = { state ->
            zoomState.setContentSize(state.painter.intrinsicSize)
        },
        modifier = Modifier
            .fillMaxSize()
            .zoomable(
                zoomState = zoomState,
                onTap = { onTap() }
            ),
        loading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = RedditOrange
                )
            }
        }
    )
}