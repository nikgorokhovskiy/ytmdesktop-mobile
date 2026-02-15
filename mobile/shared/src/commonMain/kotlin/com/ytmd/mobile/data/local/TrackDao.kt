package com.ytmd.mobile.data.local

import com.ytmd.mobile.domain.model.Track

/**
 * Data access object for cached/downloaded tracks.
 * Will be backed by SQLDelight-generated queries.
 */
interface TrackDao {

    suspend fun getAll(): List<Track>

    suspend fun getById(id: String): Track?

    suspend fun insert(track: Track)

    suspend fun insertAll(tracks: List<Track>)

    suspend fun delete(id: String)

    suspend fun deleteAll()

    suspend fun getDownloaded(): List<Track>

    suspend fun markDownloaded(id: String, localPath: String)

    suspend fun removeDownloaded(id: String)
}
