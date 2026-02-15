package com.ytmd.mobile.domain.repository

import com.ytmd.mobile.domain.model.PlayerState
import com.ytmd.mobile.domain.model.Track
import kotlinx.coroutines.flow.StateFlow

interface PlayerRepository {

    fun getPlayerState(): StateFlow<PlayerState>

    suspend fun play(track: Track)

    suspend fun pause()

    suspend fun resume()

    suspend fun next()

    suspend fun previous()

    suspend fun seekTo(positionMs: Long)

    suspend fun setQueue(tracks: List<Track>, startIndex: Int = 0)
}
