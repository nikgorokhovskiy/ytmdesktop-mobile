package com.ytmd.mobile.player

import com.ytmd.mobile.domain.model.PlayerState
import com.ytmd.mobile.domain.model.Track
import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-specific player controller.
 * Android: Media3 ExoPlayer + MediaSessionService
 * iOS: AVPlayer wrapper (called from Swift via shared framework)
 */
expect class PlayerController {

    val state: StateFlow<PlayerState>

    fun play(streamUrl: String, track: Track)

    fun pause()

    fun resume()

    fun seekTo(positionMs: Long)

    fun next()

    fun previous()

    fun setQueue(tracks: List<Track>, startIndex: Int)

    fun release()
}
