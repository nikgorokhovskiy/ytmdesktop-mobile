package com.ytmd.mobile.data.repository

import com.ytmd.mobile.data.remote.InnertubeApi
import com.ytmd.mobile.domain.model.Album
import com.ytmd.mobile.domain.model.Playlist
import com.ytmd.mobile.domain.model.SearchResult
import com.ytmd.mobile.domain.model.StreamInfo
import com.ytmd.mobile.domain.model.Track
import com.ytmd.mobile.domain.repository.MusicRepository

/**
 * Implementation of MusicRepository using Innertube API.
 * Currently contains stubs for /player and /search; browse parsing is TODO.
 */
class MusicRepositoryImpl(
    private val innertubeApi: InnertubeApi,
) : MusicRepository {

    override suspend fun search(query: String): List<SearchResult> {
        val response = innertubeApi.search(query)
        // TODO: Parse MusicShelfRenderer items into SearchResult list
        return parseSearchResponse(response)
    }

    override suspend fun getTrack(videoId: String): Track? {
        val response = innertubeApi.player(videoId)
        val details = response.videoDetails ?: return null
        return Track(
            id = details.videoId,
            title = details.title,
            artist = details.author ?: "Unknown",
            duration = details.lengthSeconds.toLongOrNull()?.times(1000) ?: 0L,
            thumbnailUrl = details.thumbnail?.thumbnails?.lastOrNull()?.url,
        )
    }

    override suspend fun getAlbum(browseId: String): Album? {
        // TODO: Parse browse response for album data
        innertubeApi.browse(browseId)
        return null
    }

    override suspend fun getPlaylist(browseId: String): Playlist? {
        // TODO: Parse browse response for playlist data
        innertubeApi.browse(browseId)
        return null
    }

    override suspend fun getHome(): List<Playlist> {
        // TODO: Parse browse response for home feed
        // browseId = "FEmusic_home"
        return emptyList()
    }

    override suspend fun getStreamInfo(videoId: String): StreamInfo? {
        val response = innertubeApi.player(videoId)
        val format = response.streamingData
            ?.adaptiveFormats
            ?.filter { it.mimeType.startsWith("audio/") }
            ?.maxByOrNull { it.bitrate }
            ?: return null

        return StreamInfo(
            url = format.url ?: return null,
            mimeType = format.mimeType,
            bitrate = format.bitrate,
            codec = extractCodec(format.mimeType),
            contentLength = format.contentLength?.toLongOrNull(),
        )
    }

    private fun extractCodec(mimeType: String): String {
        // mimeType format: "audio/webm; codecs=\"opus\""
        val codecMatch = Regex("codecs=\"(.+?)\"").find(mimeType)
        return codecMatch?.groupValues?.getOrNull(1) ?: "unknown"
    }

    private fun parseSearchResponse(
        response: com.ytmd.mobile.data.remote.SearchResponse,
    ): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val sections = response.contents
            ?.tabbedSearchResultsRenderer
            ?.tabs
            ?.firstOrNull()
            ?.tabRenderer
            ?.content
            ?.sectionListRenderer
            ?.contents
            ?: return results

        for (section in sections) {
            val shelf = section.musicShelfRenderer ?: continue
            for (item in shelf.contents) {
                val renderer = item.musicResponsiveListItemRenderer ?: continue
                val videoId = renderer.playlistItemData?.videoId
                val title = renderer.flexColumns
                    .getOrNull(0)
                    ?.musicResponsiveListItemFlexColumnRenderer
                    ?.text
                    ?.runs
                    ?.firstOrNull()
                    ?.text ?: continue

                val artist = renderer.flexColumns
                    .getOrNull(1)
                    ?.musicResponsiveListItemFlexColumnRenderer
                    ?.text
                    ?.runs
                    ?.firstOrNull()
                    ?.text ?: "Unknown"

                val thumbnailUrl = renderer.thumbnail
                    ?.musicThumbnailRenderer
                    ?.thumbnail
                    ?.thumbnails
                    ?.lastOrNull()
                    ?.url

                if (videoId != null) {
                    results.add(
                        SearchResult.TrackResult(
                            Track(
                                id = videoId,
                                title = title,
                                artist = artist,
                                thumbnailUrl = thumbnailUrl,
                            )
                        )
                    )
                }
            }
        }
        return results
    }
}
