package com.ytmd.mobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val duration: Long = 0L,
    val thumbnailUrl: String? = null,
    val streamUrl: String? = null,
)
