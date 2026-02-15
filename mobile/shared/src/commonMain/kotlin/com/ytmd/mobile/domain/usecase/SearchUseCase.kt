package com.ytmd.mobile.domain.usecase

import com.ytmd.mobile.domain.model.SearchResult
import com.ytmd.mobile.domain.repository.MusicRepository

class SearchUseCase(
    private val musicRepository: MusicRepository,
) {

    suspend operator fun invoke(query: String): Result<List<SearchResult>> {
        return try {
            val results = musicRepository.search(query)
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
