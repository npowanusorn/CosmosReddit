package com.hamburghini.cosmos.ui.screens.videoplayer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamburghini.cosmos.core.util.Logger
import com.hamburghini.playerplus.player.AbstractPlayerListener
import com.hamburghini.playerplus.player.PlayerController
import com.hamburghini.playerplus.player.configs.PlayerConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var playerController: PlayerController? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _hasEnded = MutableStateFlow(false)
    val hasEnded = _hasEnded.asStateFlow()

    private val _currentTime = MutableStateFlow(0L)
    val currentTime = _currentTime.asStateFlow()

    private val _totalTime = MutableStateFlow(0L)
    val totalTime = _totalTime.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isPlayerReady = MutableStateFlow(false)
    val isPlayerReady = _isPlayerReady.asStateFlow()

    private var wasPlayingBeforeSeek = false
    private var isSeeking = false

    fun initializePlayer(contentUrl: String) {
        if (playerController != null) {
            Logger.w("Player already initialized, skipping")
            return
        }

        try {
            playerController = PlayerController(context).apply {
                open(
                    PlayerConfig.Builder(contentUrl)
                        .setPlayWhenReady(true)
                        .build()
                )

                addPlayerListener(object : AbstractPlayerListener() {
                    override fun onPlaybackStateChanged(state: PlayerController.State) {
                        Logger.i("Playback state changed: $state")
                        when (state) {
                            PlayerController.State.READY -> {
                                Logger.i("Player READY - duration: $duration")
                                _isLoading.value = false
                                _totalTime.value = duration
                                _isPlaying.value = isPlaying
                                _isPlayerReady.value = true
                                _hasEnded.value = false
                            }
                            PlayerController.State.ENDED -> {
                                Logger.i("Player ENDED")
                                _isPlaying.value = false
                                _hasEnded.value = true
                            }
                            PlayerController.State.BUFFERING -> {
                                Logger.i("Player BUFFERING")
                                _isLoading.value = true
                            }
                            PlayerController.State.IDLE -> {
                                Logger.i("Player IDLE")
                            }
                        }
                    }
                })

                onCurrentTime(0.1) { time ->
                    if (!isSeeking) {
                        _currentTime.value = time
                    }
                }
            }
        } catch (e: Exception) {
            _isLoading.value = false
        }
    }

    fun getPlayerController(): PlayerController? = playerController

    fun play() {
        Logger.i("play() called")
        playerController?.play()
        _isPlaying.value = true
    }

    fun pause() {
        Logger.i("pause() called")
        playerController?.pause()
        _isPlaying.value = false
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun replay() {
        viewModelScope.launch {
            playerController?.seekTo(0)
            _hasEnded.value = false
            _currentTime.value = 0
            play()
        }
    }

    fun seekForward() {
        playerController?.skipForward(10_000L)
    }

    fun seekBackward() {
        playerController?.skipBackward(10_000L)
    }

    fun startSeeking() {
        isSeeking = true
        wasPlayingBeforeSeek = _isPlaying.value
        if (wasPlayingBeforeSeek) {
            pause()
        }
    }

    fun updateSeekPosition(position: Long) {
        // Update UI state while seeking
        _currentTime.value = position
    }

    fun finishSeeking(position: Long) {
        viewModelScope.launch {
            playerController?.seekTo(position)
            isSeeking = false

            // Small delay to let seek complete
            kotlinx.coroutines.delay(100)

            if (wasPlayingBeforeSeek) {
                play()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerController?.release()
        playerController = null
    }
}