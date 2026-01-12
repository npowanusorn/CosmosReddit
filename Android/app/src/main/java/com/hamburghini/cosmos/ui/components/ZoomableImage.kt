package com.hamburghini.cosmos.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage
import com.hamburghini.cosmos.ui.theme.RedditOrange

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
    onTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var shouldAnimate by remember { mutableStateOf(false) }

    // Animated scale for smooth transitions
    val animatedScale by animateFloatAsState(
        targetValue = if (shouldAnimate) scale else scale,
        animationSpec = tween(300),
        label = "scale",
        finishedListener = { shouldAnimate = false }
    )

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)

        val maxX = (scale - 1f) * 1000f
        val maxY = (scale - 1f) * 1000f

        offset = Offset(
            x = (offset.x + offsetChange.x).coerceIn(-maxX, maxX),
            y = (offset.y + offsetChange.y).coerceIn(-maxY, maxY)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = "Full size image",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = if (shouldAnimate) animatedScale else scale,
                    scaleY = if (shouldAnimate) animatedScale else scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state = transformableState)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onDoubleTap = { tapOffset ->
                            shouldAnimate = true
                            if (scale > 1f) {
                                // Reset to normal
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                // Zoom to 2x at tap location
                                scale = 2f

                                // Calculate offset to center on tap point
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                offset = Offset(
                                    x = (centerX - tapOffset.x) * scale,
                                    y = (centerY - tapOffset.y) * scale
                                )
                            }
                        }
                    )
                },
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = RedditOrange
                    )
                }
            },
            error = {
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
}