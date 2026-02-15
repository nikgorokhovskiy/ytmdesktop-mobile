package com.ytmd.mobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StreamInfo(
    val url: String,
    val mimeType: String,
    val bitrate: Int,
    val codec: String,
    val contentLength: Long? = null,
)
