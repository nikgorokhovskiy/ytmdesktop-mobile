package com.ytmd.mobile.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ytmd.mobile.domain.model.PlayerState
import com.ytmd.mobile.domain.model.RepeatMode
import com.ytmd.mobile.domain.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Android actual implementation of PlayerController using Media3 ExoPlayer.
 * In production, the ExoPlayer instance lives in MusicService (MediaSessionService).
 */
actual class PlayerController(
    context: Context,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _state = MutableStateFlow(PlayerState())
    actual val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var queue: List<Track> = emptyList()
    private var currentIndex: Int = -1

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState { copy(isPlaying = isPlaying) }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    next()
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                val mode = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
                updateState { copy(repeatMode = mode) }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                updateState { copy(shuffleEnabled = shuffleModeEnabled) }
            }
        })

        // Progress updater
        scope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    updateState {
                        copy(
                            progressMs = exoPlayer.currentPosition,
                            durationMs = exoPlayer.duration.coerceAtLeast(0),
                        )
                    }
                }
                delay(500)
            }
        }
    }

    actual fun play(streamUrl: String, track: Track) {
        val mediaItem = MediaItem.fromUri(streamUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
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
        exoPlayer.pause()
    }

    actual fun resume() {
        exoPlayer.play()
    }

    actual fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        updateState { copy(progressMs = positionMs) }
    }

    actual fun next() {
        if (queue.isEmpty() || currentIndex < 0) return
        currentIndex = (currentIndex + 1) % queue.size
        val nextTrack = queue[currentIndex]
        // In full impl, would fetch stream URL and play
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
        exoPlayer.release()
    }

    private inline fun updateState(transform: PlayerState.() -> PlayerState) {
        _state.value = _state.value.transform()
    }
}
