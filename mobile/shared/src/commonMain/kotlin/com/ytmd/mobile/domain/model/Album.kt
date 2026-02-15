package com.ytmd.mobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String? = null,
    val tracks: List<Track> = emptyList(),
)
