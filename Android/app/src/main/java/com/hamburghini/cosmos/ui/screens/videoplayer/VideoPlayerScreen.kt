package com.hamburghini.cosmos.ui.screens.videoplayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.hamburghini.cosmos.core.util.Logger
import com.hamburghini.playerplus.player.PlayerView
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VideoPlayerScreen(
    contentUrl: String,
    onBackClick: () -> Unit,
    viewModel: VideoPlayerViewModel = hiltViewModel()
) {
    Logger.i("VideoPlayerScreen composing with contentUrl: $contentUrl")

    val context = LocalContext.current
    val isPlaying by viewModel.isPlaying.collectAsState()
    val hasEnded by viewModel.hasEnded.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPlayerReady by viewModel.isPlayerReady.collectAsState()

    var isSeeking by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }

    // Auto-hide controls after 3 seconds when playing
    LaunchedEffect(isPlaying, showControls) {
        if (isPlaying && showControls && !hasEnded) {
            delay(3000)
            showControls = false
        }
    }

    // Initialize player when URL is available
    LaunchedEffect(contentUrl) {
        Logger.i("LaunchedEffect: Initializing player with URL: $contentUrl")
        viewModel.initializePlayer(contentUrl)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
        // Only show player when it's ready
        if (isPlayerReady) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    Logger.i("AndroidView factory: Creating PlayerView")
                    PlayerView(context).apply {
                        val controller = viewModel.getPlayerController()
                        if (controller != null) {
                            this.controller = controller
                            Logger.i("PlayerView created with controller")
                        } else {
                            Logger.e("PlayerController is null in AndroidView factory!")
                        }
                    }
                },
                onRelease = {
                    Logger.i("AndroidView: onRelease called")
                }
            )
        }

        // Loading Indicator
        if (isLoading || !isPlayerReady) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        }

        // Media Controls with fade animation
        AnimatedVisibility(
            visible = showControls && isPlayerReady,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopStart
            ) {
                IconButton(
                    onClick = onBackClick
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                MediaControlCard(
                    isPlaying = isPlaying,
                    hasEnded = hasEnded,
                    currentTime = currentTime,
                    totalTime = totalTime,
                    onPlayPause = {
                        viewModel.togglePlayPause()
                        showControls = true
                    },
                    onReplay = {
                        viewModel.replay()
                        showControls = true
                    },
                    onSeekBack = {
                        viewModel.seekBackward()
                        showControls = true
                    },
                    onSeekForward = {
                        viewModel.seekForward()
                        showControls = true
                    },
                    onValueChanged = { value ->
                        if (!isSeeking) {
                            viewModel.startSeeking()
                            isSeeking = true
                        }
                        viewModel.updateSeekPosition(value.toLong())
                        showControls = true
                    },
                    onValueChangeFinished = {
                        viewModel.finishSeeking(currentTime)
                        isSeeking = false
                    }
                )
            }
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
    onPlayPause: () -> Unit,
    onReplay: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onValueChanged: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
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
                    value = currentTime.toFloat(),
                    onValueChange = onValueChanged,
                    onValueChangeFinished = onValueChangeFinished,
                    valueRange = if (totalTime > 0) 0f..totalTime.toFloat() else 0f..1f,
                    steps = 0,
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
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
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
        onValueChanged = {},
        onValueChangeFinished = {}
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
        onValueChanged = {},
        onValueChangeFinished = {}
    )
}