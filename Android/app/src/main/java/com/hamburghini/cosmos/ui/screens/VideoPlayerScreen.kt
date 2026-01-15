package com.hamburghini.cosmos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.hamburghini.cosmos.viewmodel.VideoPlayerViewModel
import com.hamburghini.playerplus.player.AbstractPlayerListener
import com.hamburghini.playerplus.player.PlayerController
import com.hamburghini.playerplus.player.PlayerView
import com.hamburghini.playerplus.player.configs.PlayerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import com.hamburghini.cosmos.viewmodel.SeekEvent

@Composable
fun VideoPlayerScreen(
    contentUrl: String,
    viewModel: VideoPlayerViewModel = hiltViewModel()
) {
    Box(modifier = Modifier.fillMaxSize()) {

        VideoPlayerView(
            contentUrl = contentUrl,
            isPlayingFlow = viewModel.isPlaying,
            seekEvents = viewModel.seekEvents,
            onVideoEnded = {
                viewModel.pause()
            }
        )

        MediaControlCard(
            onPlayPause = {
                if (viewModel.isPlaying.value) {
                    viewModel.pause()
                } else {
                    viewModel.play()
                }
            },
            isPlaying = viewModel.isPlaying.collectAsState().value,
            onSeekBack = { viewModel.seekBackward() },
            onSeekForward = { viewModel.seekForward() }
        )
    }
}

@Composable
private fun VideoPlayerView(
    contentUrl: String,
    isPlayingFlow: StateFlow<Boolean>,
    seekEvents: SharedFlow<SeekEvent>,
    onVideoEnded: () -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val controller = PlayerController(context)

            controller.open(
                PlayerConfig.Builder(contentUrl)
                    .setPlayWhenReady(true)
                    .build()
            )

            controller.addPlayerListener(object : AbstractPlayerListener() {
                override fun onPlaybackStateChanged(state: PlayerController.State) {
                    if (state == PlayerController.State.ENDED) {
                        onVideoEnded()
                    }
                }
            })

            val playerView = PlayerView(context)
            playerView.controller = controller

            // Observe Compose state
            CoroutineScope(Dispatchers.Main).launch {
                isPlayingFlow.collect { playing ->
                    if (playing) controller.play() else controller.pause()
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                seekEvents.collect { event ->
                    when (event) {
                        is SeekEvent.Forward ->
                            controller.skipForward(10_000L)

                        is SeekEvent.Backward ->
                            controller.skipBackward(10_000L)
                    }
                }
            }

            playerView
        },
        onRelease = {
            // VERY important
            it.controller?.release()
        }
    )
}

@Composable
fun MediaControlCard(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onSeekBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Replay10,
                    contentDescription = "Rewind 10 seconds"
                )
            }

            FilledIconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying)
                        Icons.Default.Pause
                    else
                        Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }

            IconButton(
                onClick = onSeekForward,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Forward10,
                    contentDescription = "Forward 10 seconds"
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewMediaControlCard() {
    MediaControlCard(
        isPlaying = true,
        onPlayPause = {},
        onSeekForward = {},
        onSeekBack = {}
    )
}