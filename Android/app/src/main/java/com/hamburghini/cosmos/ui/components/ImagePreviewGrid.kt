package com.hamburghini.cosmos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.decode.BitmapFactoryDecoder

@Composable
fun ImagePreviewGrid(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit = {}
) {
    when (imageUrls.size) {
        1 -> OneImage(imageUrls[0], modifier, onClick)
        2 -> TwoImages(imageUrls, modifier, onClick)
        else -> ThreePlusImages(imageUrls, modifier, onClick)
    }
}

@Composable
private fun OneImage(
    url: String,
    modifier: Modifier,
    onClick: (Int) -> Unit
) {
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = { onClick(0) }),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun TwoImages(
    urls: List<String>,
    modifier: Modifier,
    onClick: (Int) -> Unit
) {
    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            add(BitmapFactoryDecoder.Factory())
        }
        .build()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            urls.take(2).forEachIndexed { idx, url ->
                AsyncImage(
                    model = url,
                    imageLoader = imageLoader,
                    contentDescription = null,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            onClick = { onClick(idx) }
                        ),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            ImageCountBox(count = urls.size)
        }
    }
}

@Composable
private fun ThreePlusImages(
    urls: List<String>,
    modifier: Modifier,
    onClick: (Int) -> Unit
) {
    val gap = 2.dp
    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            add(BitmapFactoryDecoder.Factory())
        }
        .build()

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
            AsyncImage(
                model = urls[0],
                imageLoader = imageLoader,
                contentDescription = null,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        onClick = { onClick(0) }
                    ),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {

                // Top-right image
                AsyncImage(
                    model = urls[1],
                    imageLoader = imageLoader,
                    contentDescription = null,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable(
                            onClick = { onClick(1) }
                        ),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AsyncImage(
                        model = urls[2],
                        imageLoader = imageLoader,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onClick(2) },
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        ImageCountBox(count = urls.size)
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageCountBox(
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
private fun ImageCountBoxPreview() {
    ImageCountBox(5)
}