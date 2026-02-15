package com.ytmd.mobile.data.remote

import com.ytmd.mobile.domain.repository.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Innertube API client emulating ANDROID_MUSIC client.
 * Uses Ktor HttpClient for multiplatform HTTP requests.
 */
class InnertubeApi(
    private val httpClient: HttpClient,
    private val authRepository: AuthRepository,
) {

    private suspend fun getHeaders(): Map<String, String> {
        return buildMap {
            put("User-Agent", InnertubeEndpoints.USER_AGENT)
            putAll(authRepository.buildAuthHeaders())
        }
    }

    /**
     * Fetch player data for a video (stream URLs, metadata).
     */
    suspend fun player(videoId: String): PlayerResponse {
        val request = PlayerRequest(videoId = videoId)
        return httpClient.post(InnertubeEndpoints.BASE_URL + InnertubeEndpoints.PLAYER) {
            contentType(ContentType.Application.Json)
            header("X-Goog-Api-Key", InnertubeEndpoints.API_KEY)
            getHeaders().forEach { (key, value) -> header(key, value) }
            setBody(request)
        }.body()
    }

    /**
     * Search YouTube Music.
     */
    suspend fun search(query: String, params: String? = null): SearchResponse {
        val request = SearchRequest(query = query, params = params)
        return httpClient.post(InnertubeEndpoints.BASE_URL + InnertubeEndpoints.SEARCH) {
            contentType(ContentType.Application.Json)
            header("X-Goog-Api-Key", InnertubeEndpoints.API_KEY)
            getHeaders().forEach { (key, value) -> header(key, value) }
            setBody(request)
        }.body()
    }

    /**
     * Browse content (albums, playlists, artist pages, home feed).
     */
    suspend fun browse(browseId: String): String {
        val request = BrowseRequest(browseId = browseId)
        return httpClient.post(InnertubeEndpoints.BASE_URL + InnertubeEndpoints.BROWSE) {
            contentType(ContentType.Application.Json)
            header("X-Goog-Api-Key", InnertubeEndpoints.API_KEY)
            getHeaders().forEach { (key, value) -> header(key, value) }
            setBody(request)
        }.body()
    }
}
