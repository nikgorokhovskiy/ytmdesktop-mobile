package com.ytmd.mobile.player

import com.ytmd.mobile.domain.model.PlayerState
import com.ytmd.mobile.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * iOS actual implementation of PlayerController.
 * Bridges to AVPlayer via Swift interop (PlayerBridge.swift).
 *
 * The actual AVPlayer setup and control happens in Swift code.
 * This Kotlin class exposes state as StateFlow for the shared layer,
 * and provides methods that will be called from Swift via the shared framework.
 */
actual class PlayerController {

    private val _state = MutableStateFlow(PlayerState())
    actual val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var queue: List<Track> = emptyList()
    private var currentIndex: Int = -1

    actual fun play(streamUrl: String, track: Track) {
        // Called from Swift: PlayerBridge will set up AVPlayer with the URL
        updateState {
            copy(
                currentTrack = track,
                isPlaying = true,
                progressMs = 0,
                durationMs = track.duration,
            )
        }
    }

    actual fun pause() {
        updateState { copy(isPlaying = false) }
    }

    actual fun resume() {
        updateState { copy(isPlaying = true) }
    }

    actual fun seekTo(positionMs: Long) {
        updateState { copy(progressMs = positionMs) }
    }

    actual fun next() {
        if (queue.isEmpty() || currentIndex < 0) return
        currentIndex = (currentIndex + 1) % queue.size
        val nextTrack = queue[currentIndex]
        updateState { copy(currentTrack = nextTrack, progressMs = 0) }
    }

    actual fun previous() {
        if (queue.isEmpty() || currentIndex < 0) return
        currentIndex = if (currentIndex - 1 < 0) queue.size - 1 else currentIndex - 1
        val prevTrack = queue[currentIndex]
        updateState { copy(currentTrack = prevTrack, progressMs = 0) }
    }

    actual fun setQueue(tracks: List<Track>, startIndex: Int) {
        queue = tracks
        currentIndex = startIndex
        updateState { copy(queue = tracks) }
    }

    actual fun release() {
        // Called from Swift when the app is terminating
        updateState { PlayerState() }
    }

    // --- Methods called from Swift to update state ---

    fun updateProgress(positionMs: Long, durationMs: Long) {
        updateState { copy(progressMs = positionMs, durationMs = durationMs) }
    }

    fun updatePlaybackState(isPlaying: Boolean) {
        updateState { copy(isPlaying = isPlaying) }
    }

    private inline fun updateState(transform: PlayerState.() -> PlayerState) {
        _state.value = _state.value.transform()
    }
}
