package com.ytmd.mobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class SearchResult {

    @Serializable
    data class TrackResult(val track: Track) : SearchResult()

    @Serializable
    data class AlbumResult(val album: Album) : SearchResult()

    @Serializable
    data class ArtistResult(
        val id: String,
        val name: String,
        val thumbnailUrl: String? = null,
    ) : SearchResult()

    @Serializable
    data class PlaylistResult(val playlist: Playlist) : SearchResult()
}
