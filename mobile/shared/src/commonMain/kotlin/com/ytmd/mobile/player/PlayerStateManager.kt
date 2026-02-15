package com.ytmd.mobile.player

import com.ytmd.mobile.domain.model.PlayerState
import com.ytmd.mobile.domain.model.Track
import com.ytmd.mobile.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Bridges the platform PlayerController with the domain PlayerRepository interface.
 * Allows use cases to interact with the player without knowing platform details.
 */
class PlayerStateManager(
    private val playerController: PlayerController,
    private val getStreamUrl: suspend (String) -> String?,
) : PlayerRepository {

    override fun getPlayerState(): StateFlow<PlayerState> = playerController.state

    override suspend fun play(track: Track) {
        val streamUrl = track.streamUrl ?: getStreamUrl(track.id) ?: return
        playerController.play(streamUrl, track)
    }

    override suspend fun pause() {
        playerController.pause()
    }

    override suspend fun resume() {
        playerController.resume()
    }

    override suspend fun next() {
        playerController.next()
    }

    override suspend fun previous() {
        playerController.previous()
    }

    override suspend fun seekTo(positionMs: Long) {
        playerController.seekTo(positionMs)
    }

    override suspend fun setQueue(tracks: List<Track>, startIndex: Int) {
        playerController.setQueue(tracks, startIndex)
    }
}
