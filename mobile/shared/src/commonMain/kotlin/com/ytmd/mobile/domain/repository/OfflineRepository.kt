package com.ytmd.mobile.domain.repository

import com.ytmd.mobile.domain.model.DownloadState
import com.ytmd.mobile.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface OfflineRepository {

    suspend fun download(track: Track): Flow<DownloadState>

    suspend fun getDownloaded(): List<Track>

    suspend fun deleteDownload(trackId: String)

    suspend fun isDownloaded(trackId: String): Boolean
}
