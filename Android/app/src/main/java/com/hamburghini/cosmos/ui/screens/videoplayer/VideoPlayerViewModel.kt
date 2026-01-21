package com.hamburghini.cosmos.ui.screens.videoplayer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val exoPlayer = ExoPlayer.Builder(context).build()

    private val _uiState = MutableStateFlow(VideoPlayerUiState())
    val uiState: StateFlow<VideoPlayerUiState> = _uiState.asStateFlow()

    private var hideControlsJob: Job? = null
    private var wasPlayingBeforeScrub = false

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update {
                    it.copy(isPlaying = isPlaying)
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                _uiState.update {
                    it.copy(
                        hasEnded = state == Player.STATE_ENDED,
                        totalTime = exoPlayer.duration.coerceAtLeast(0L)
                    )
                }
            }
        })

        startProgressUpdates()
    }

    fun prepare(url: String) {
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        showControls(autoHide = true)
    }

    fun player(): ExoPlayer = exoPlayer

    private fun startProgressUpdates() {
        viewModelScope.launch {
            while (true) {
                if (!_uiState.value.isScrubbing) {
                    _uiState.update {
                        it.copy(currentTime = exoPlayer.currentPosition)
                    }
                }
                delay(100)
            }
        }
    }

    /* ───────── Controls ───────── */

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
        showControls(autoHide = true)
    }

    fun replay() {
        exoPlayer.seekTo(0)
        exoPlayer.play()
    }

    fun seekBack() {
        exoPlayer.seekTo((exoPlayer.currentPosition - 10_000).coerceAtLeast(0))
        showControls(autoHide = true)
    }

    fun seekForward() {
        exoPlayer.seekTo(exoPlayer.currentPosition + 10_000)
        showControls(autoHide = true)
    }

    fun seekTo(position: Float) {
        exoPlayer.seekTo(position.toLong())
    }

    fun showControls(autoHide: Boolean) {
        _uiState.update { it.copy(controlsVisible = true) }

        hideControlsJob?.cancel()
        if (autoHide) {
            hideControlsJob = viewModelScope.launch {
                delay(3_000)
                _uiState.update { it.copy(controlsVisible = false) }
            }
        }
    }

    fun toggleControls() {
        val visible = !_uiState.value.controlsVisible
        _uiState.update { it.copy(controlsVisible = visible) }
        if (visible) showControls(autoHide = true)
    }

    fun onScrubStart() {
        wasPlayingBeforeScrub = exoPlayer.isPlaying
        exoPlayer.pause()

        _uiState.update {
            it.copy(
                isScrubbing = true,
                scrubPosition = it.currentTime
            )
        }
    }

    fun onScrubMove(position: Float) {
        _uiState.update {
            it.copy(scrubPosition = position.toLong())
        }
    }

    fun onScrubEnd() {
        val seekTo = _uiState.value.scrubPosition
        exoPlayer.seekTo(seekTo)

        if (wasPlayingBeforeScrub) {
            exoPlayer.play()
        }

        _uiState.update {
            it.copy(
                isScrubbing = false,
                currentTime = seekTo
            )
        }

        showControls(autoHide = true)
    }

    override fun onCleared() {
        exoPlayer.release()
    }
}

data class VideoPlayerUiState(
    val isPlaying: Boolean = false,
    val hasEnded: Boolean = false,
    val currentTime: Long = 0L,
    val totalTime: Long = 0L,
    val controlsVisible: Boolean = true,
    val isScrubbing: Boolean = false,
    val scrubPosition: Long = 0L
)
