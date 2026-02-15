package com.ytmd.mobile.domain.repository

import com.ytmd.mobile.domain.model.Album
import com.ytmd.mobile.domain.model.Playlist
import com.ytmd.mobile.domain.model.SearchResult
import com.ytmd.mobile.domain.model.StreamInfo
import com.ytmd.mobile.domain.model.Track

interface MusicRepository {

    suspend fun search(query: String): List<SearchResult>

    suspend fun getTrack(videoId: String): Track?

    suspend fun getAlbum(browseId: String): Album?

    suspend fun getPlaylist(browseId: String): Playlist?

    suspend fun getHome(): List<Playlist>

    suspend fun getStreamInfo(videoId: String): StreamInfo?
}
