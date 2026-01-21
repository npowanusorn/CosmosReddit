package com.hamburghini.cosmos.ui.screens.videoplayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.ui.compose.PlayerSurface
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun VideoPlayerScreen(
    contentUrl: String,
    aspectRatio: Float,
    onBackClick: () -> Unit,
    viewModel: VideoPlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(contentUrl) {
        viewModel.prepare(contentUrl)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures {
                    viewModel.toggleControls()
                }
            }
    ) {
        /* ───────── Video ───────── */

        PlayerSurface(
            player = viewModel.player(),
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(aspectRatio)
        )

        /* ───────── Back Button ───────── */

        AnimatedVisibility(
            visible = uiState.controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.4f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        /* ───────── Controls ───────── */

        AnimatedVisibility(
            visible = uiState.controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            MediaControlCard(
                isPlaying = uiState.isPlaying,
                hasEnded = uiState.hasEnded,
                currentTime = uiState.currentTime,
                totalTime = uiState.totalTime,
                isScrubbing = uiState.isScrubbing,
                scrubPosition = uiState.scrubPosition,
                onScrubStart = viewModel::onScrubStart,
                onScrubMove = viewModel::onScrubMove,
                onScrubEnd = viewModel::onScrubEnd,
                onPlayPause = viewModel::togglePlayPause,
                onReplay = viewModel::replay,
                onSeekBack = viewModel::seekBack,
                onSeekForward = viewModel::seekForward,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaControlCard(
    isPlaying: Boolean,
    hasEnded: Boolean,
    currentTime: Long,
    totalTime: Long,
    isScrubbing: Boolean,
    scrubPosition: Long,
    onPlayPause: () -> Unit,
    onReplay: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onScrubStart: () -> Unit,
    onScrubMove: (Float) -> Unit,
    onScrubEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .blur(20.dp)
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(
                vertical = 8.dp,
                horizontal = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onSeekBack) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 10 seconds",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Show replay button when ended, otherwise show play/pause
                IconButton(onClick = if (hasEnded) onReplay else onPlayPause) {
                    Icon(
                        imageVector = when {
                            hasEnded -> Icons.Default.Replay
                            isPlaying -> Icons.Rounded.Pause
                            else -> Icons.Rounded.PlayArrow
                        },
                        contentDescription = when {
                            hasEnded -> "Replay"
                            isPlaying -> "Pause"
                            else -> "Play"
                        },
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(onClick = onSeekForward) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Forward 10 seconds",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatMilliseconds(currentTime),
                    style = MaterialTheme.typography.bodySmall
                )

                Slider(
                    value = if (isScrubbing) scrubPosition.toFloat() else currentTime.toFloat(),
                    onValueChange = { value ->
                        if (!isScrubbing) onScrubStart()
                        onScrubMove(value)
                    },
                    onValueChangeFinished = {
                        onScrubEnd()
                    },
                    valueRange = if (totalTime > 0) {
                        0f..totalTime.toFloat()
                    } else {
                        0f..1f
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    thumb = {
                        SliderDefaults.Thumb(
                            interactionSource = remember { MutableInteractionSource() },
                            thumbSize = DpSize(20.dp, 20.dp)
                        )
                    },
                    track = {
                        SliderDefaults.Track(
                            sliderState = it,
                            drawStopIndicator = null,
                            thumbTrackGapSize = 0.dp,
                            modifier = Modifier.height(5.dp)
                        )
                    }
                )

                Text(
                    text = formatMilliseconds(totalTime),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun formatMilliseconds(ms: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}

@Preview
@Composable
fun PreviewMediaControlCard() {
    MediaControlCard(
        isPlaying = true,
        hasEnded = false,
        onPlayPause = {},
        onReplay = {},
        onSeekForward = {},
        onSeekBack = {},
        currentTime = 5000,
        totalTime = 10000,
        isScrubbing = false,
        scrubPosition = 0,
        onScrubStart = {},
        onScrubMove = {},
        onScrubEnd = {}
    )
}

@Preview
@Composable
fun PreviewMediaControlCardEnded() {
    MediaControlCard(
        isPlaying = false,
        hasEnded = true,
        onPlayPause = {},
        onReplay = {},
        onSeekForward = {},
        onSeekBack = {},
        currentTime = 10000,
        totalTime = 10000,
        isScrubbing = false,
        scrubPosition = 0,
        onScrubStart = {},
        onScrubMove = {},
        onScrubEnd = {}
    )
}