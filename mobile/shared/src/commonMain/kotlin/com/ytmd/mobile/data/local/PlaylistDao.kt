package com.ytmd.mobile.data.local

import com.ytmd.mobile.domain.model.Playlist

/**
 * Data access object for cached playlists.
 * Will be backed by SQLDelight-generated queries.
 */
interface PlaylistDao {

    suspend fun getAll(): List<Playlist>

    suspend fun getById(id: String): Playlist?

    suspend fun insert(playlist: Playlist)

    suspend fun delete(id: String)

    suspend fun deleteAll()
}
