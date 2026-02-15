package com.ytmd.mobile.domain.usecase

import com.ytmd.mobile.domain.model.Playlist
import com.ytmd.mobile.domain.repository.MusicRepository

class GetPlaylistsUseCase(
    private val musicRepository: MusicRepository,
) {

    suspend operator fun invoke(): Result<List<Playlist>> {
        return try {
            val playlists = musicRepository.getHome()
            Result.success(playlists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
