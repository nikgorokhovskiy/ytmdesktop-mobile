package com.ytmd.mobile.data.repository

import com.ytmd.mobile.data.local.TrackDao
import com.ytmd.mobile.domain.model.DownloadState
import com.ytmd.mobile.domain.model.Track
import com.ytmd.mobile.domain.repository.OfflineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementation of OfflineRepository.
 * Manages track downloads and offline availability.
 * Full download logic (HTTP streaming to disk) is TODO.
 */
class OfflineRepositoryImpl(
    private val trackDao: TrackDao,
) : OfflineRepository {

    override suspend fun download(track: Track): Flow<DownloadState> = flow {
        emit(DownloadState.Downloading(0f))
        // TODO: Implement actual download via Ktor streaming
        // 1. Get stream URL from MusicRepository
        // 2. Download to platform-specific storage
        // 3. Emit progress updates
        // 4. On completion, save to DB
        emit(DownloadState.Failed("Download not yet implemented"))
    }

    override suspend fun getDownloaded(): List<Track> {
        return trackDao.getDownloaded()
    }

    override suspend fun deleteDownload(trackId: String) {
        // TODO: Delete file from disk
        trackDao.removeDownloaded(trackId)
    }

    override suspend fun isDownloaded(trackId: String): Boolean {
        return trackDao.getDownloaded().any { it.id == trackId }
    }
}
