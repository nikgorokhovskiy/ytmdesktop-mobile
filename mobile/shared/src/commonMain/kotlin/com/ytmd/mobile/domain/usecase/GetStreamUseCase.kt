package com.ytmd.mobile.domain.usecase

import com.ytmd.mobile.domain.model.StreamInfo
import com.ytmd.mobile.domain.repository.MusicRepository

class GetStreamUseCase(
    private val musicRepository: MusicRepository,
) {

    suspend operator fun invoke(videoId: String): Result<StreamInfo> {
        return try {
            val streamInfo = musicRepository.getStreamInfo(videoId)
            if (streamInfo != null) {
                Result.success(streamInfo)
            } else {
                Result.failure(Exception("Stream not found for video: $videoId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
