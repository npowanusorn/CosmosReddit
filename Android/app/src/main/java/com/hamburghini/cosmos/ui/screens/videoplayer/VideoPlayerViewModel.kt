package com.hamburghini.cosmos.ui.screens.videoplayer

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class VideoPlayerViewModel @Inject constructor() : ViewModel() {

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying = _isPlaying.asStateFlow()

    private val _seekEvents = MutableSharedFlow<SeekEvent>(
        extraBufferCapacity = 1
    )
    val seekEvents = _seekEvents.asSharedFlow()

    fun play() {
        _isPlaying.value = true
    }

    fun pause() {
        _isPlaying.value = false
    }

    fun seekForward() {
        _seekEvents.tryEmit(SeekEvent.Forward)
    }

    fun seekBackward() {
        _seekEvents.tryEmit(SeekEvent.Backward)
    }
}

sealed interface SeekEvent {
    object Forward : SeekEvent
    object Backward : SeekEvent
}
