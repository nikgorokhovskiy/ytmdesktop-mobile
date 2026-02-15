package com.ytmd.mobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val id: String,
    val title: String,
    val thumbnailUrl: String? = null,
    val trackCount: Int = 0,
)
