package com.hamburghini.cosmos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hamburghini.cosmos.core.util.RedditImage

@Composable
fun ImagePreviewGrid(
    redditImages: List<RedditImage>,
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit = {}
) {
    when (redditImages.size) {
        1 -> OneImage(redditImages[0], modifier, onClick)
        2 -> TwoImages(redditImages, modifier, onClick)
        else -> ThreePlusImages(redditImages, modifier, onClick)
    }
}

@Composable
private fun OneImage(
    redditImage: RedditImage,
    modifier: Modifier,
    onClick: (Int) -> Unit
) {
    RedditAsyncImage(
        redditImage = redditImage,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(redditImage.width.toFloat() / redditImage.height.toFloat())
            .clip(RoundedCornerShape(12.dp)),
        onClick = { onClick(0) }
    )
}

@Composable
private fun TwoImages(
    redditImages: List<RedditImage>,
    modifier: Modifier,
    onClick: (Int) -> Unit
) {
    val gap = 2.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            redditImages.take(2).forEachIndexed { idx, image ->
                RedditAsyncImage(
                    redditImage = image,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { onClick(idx) }
                )
            }
        }

        ImageCountOverlay(count = redditImages.size)
    }
}

@Composable
private fun ThreePlusImages(
    redditImages: List<RedditImage>,
    modifier: Modifier,
    onClick: (Int) -> Unit
) {
    val gap = 2.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {

            // Left large image
            RedditAsyncImage(
                redditImage = redditImages[0],
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = { onClick(0) }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {

                // Top-right
                RedditAsyncImage(
                    redditImage = redditImages[1],
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    onClick = { onClick(1) }
                )

                // Bottom-right
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    RedditAsyncImage(
                        redditImage = redditImages[2],
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onClick = { onClick(2) }
                    )

                    ImageCountOverlay(count = redditImages.size)
                }
            }
        }
    }
}

@Composable
private fun ImageCountOverlay(
    count: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
    ) {
        Text(
            text = "$count IMAGES",
            color = Color.Black,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Preview
@Composable
private fun ImageCountOverlayPreview() {
    ImageCountOverlay(5)
}