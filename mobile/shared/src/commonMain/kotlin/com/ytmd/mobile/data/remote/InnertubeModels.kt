package com.ytmd.mobile.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request/response models for the Innertube API.
 * These mirror the JSON structure of the YouTube Music internal API.
 */

// === Request Models ===

@Serializable
data class InnertubeContext(
    val client: ClientContext,
)

@Serializable
data class ClientContext(
    val clientName: String = InnertubeEndpoints.CLIENT_NAME,
    val clientVersion: String = InnertubeEndpoints.CLIENT_VERSION,
    @SerialName("hl") val language: String = "en",
    @SerialName("gl") val country: String = "US",
    val platform: String = "MOBILE",
    val androidSdkVersion: Int = 34,
    val userAgent: String = InnertubeEndpoints.USER_AGENT,
)

@Serializable
data class PlayerRequest(
    val videoId: String,
    val context: InnertubeContext = InnertubeContext(ClientContext()),
)

@Serializable
data class SearchRequest(
    val query: String,
    val context: InnertubeContext = InnertubeContext(ClientContext()),
    val params: String? = null, // Filter params (songs, albums, etc.)
)

@Serializable
data class BrowseRequest(
    val browseId: String,
    val context: InnertubeContext = InnertubeContext(ClientContext()),
)

// === Response Models ===

@Serializable
data class PlayerResponse(
    val videoDetails: VideoDetails? = null,
    val streamingData: StreamingData? = null,
    val playabilityStatus: PlayabilityStatus? = null,
)

@Serializable
data class VideoDetails(
    val videoId: String = "",
    val title: String = "",
    val lengthSeconds: String = "0",
    val channelId: String? = null,
    val shortDescription: String? = null,
    val thumbnail: ThumbnailContainer? = null,
    val author: String? = null,
)

@Serializable
data class ThumbnailContainer(
    val thumbnails: List<Thumbnail> = emptyList(),
)

@Serializable
data class Thumbnail(
    val url: String = "",
    val width: Int = 0,
    val height: Int = 0,
)

@Serializable
data class StreamingData(
    val adaptiveFormats: List<AdaptiveFormat> = emptyList(),
    val expiresInSeconds: String? = null,
)

@Serializable
data class AdaptiveFormat(
    val itag: Int = 0,
    val url: String? = null,
    val mimeType: String = "",
    val bitrate: Int = 0,
    val contentLength: String? = null,
    val audioQuality: String? = null,
    val audioSampleRate: String? = null,
    val audioChannels: Int? = null,
)

@Serializable
data class PlayabilityStatus(
    val status: String = "",
    val reason: String? = null,
)

@Serializable
data class SearchResponse(
    val contents: SearchContents? = null,
)

@Serializable
data class SearchContents(
    val tabbedSearchResultsRenderer: TabbedSearchResultsRenderer? = null,
)

@Serializable
data class TabbedSearchResultsRenderer(
    val tabs: List<SearchTab> = emptyList(),
)

@Serializable
data class SearchTab(
    val tabRenderer: TabRenderer? = null,
)

@Serializable
data class TabRenderer(
    val content: TabContent? = null,
)

@Serializable
data class TabContent(
    val sectionListRenderer: SectionListRenderer? = null,
)

@Serializable
data class SectionListRenderer(
    val contents: List<SectionContent> = emptyList(),
)

@Serializable
data class SectionContent(
    val musicShelfRenderer: MusicShelfRenderer? = null,
)

@Serializable
data class MusicShelfRenderer(
    val title: RunsContainer? = null,
    val contents: List<MusicShelfItem> = emptyList(),
)

@Serializable
data class MusicShelfItem(
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null,
)

@Serializable
data class MusicResponsiveListItemRenderer(
    val flexColumns: List<FlexColumn> = emptyList(),
    val thumbnail: MusicThumbnailRenderer? = null,
    val overlay: OverlayRenderer? = null,
    val playlistItemData: PlaylistItemData? = null,
)

@Serializable
data class FlexColumn(
    val musicResponsiveListItemFlexColumnRenderer: FlexColumnRenderer? = null,
)

@Serializable
data class FlexColumnRenderer(
    val text: RunsContainer? = null,
)

@Serializable
data class RunsContainer(
    val runs: List<Run> = emptyList(),
)

@Serializable
data class Run(
    val text: String = "",
    val navigationEndpoint: NavigationEndpoint? = null,
)

@Serializable
data class NavigationEndpoint(
    val browseEndpoint: BrowseEndpoint? = null,
    val watchEndpoint: WatchEndpoint? = null,
)

@Serializable
data class BrowseEndpoint(
    val browseId: String = "",
)

@Serializable
data class WatchEndpoint(
    val videoId: String = "",
    val playlistId: String? = null,
)

@Serializable
data class MusicThumbnailRenderer(
    val musicThumbnailRenderer: ThumbnailRendererInner? = null,
)

@Serializable
data class ThumbnailRendererInner(
    val thumbnail: ThumbnailContainer? = null,
)

@Serializable
data class OverlayRenderer(
    val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRenderer? = null,
)

@Serializable
data class MusicItemThumbnailOverlayRenderer(
    val content: OverlayContent? = null,
)

@Serializable
data class OverlayContent(
    val musicPlayButtonRenderer: MusicPlayButtonRenderer? = null,
)

@Serializable
data class MusicPlayButtonRenderer(
    val playNavigationEndpoint: NavigationEndpoint? = null,
)

@Serializable
data class PlaylistItemData(
    val videoId: String? = null,
)
