package com.hamburghini.cosmos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.hamburghini.cosmos.core.util.RedditImage

@Composable
fun RedditAsyncImage(
    redditImage: RedditImage,
    modifier: Modifier = Modifier,
    contentScale: ContentScale,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(redditImage.fullUrl)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier.clickable(onClick = onClick),
        loading = {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(redditImage.placeholderUrl)
                    .crossfade(false)
                    .build(),
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(20.dp)
            )
        }
    )
}
