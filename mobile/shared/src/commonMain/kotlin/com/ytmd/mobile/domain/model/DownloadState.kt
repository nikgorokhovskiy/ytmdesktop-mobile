package com.ytmd.mobile.domain.model

sealed class DownloadState {

    data object NotDownloaded : DownloadState()

    data class Downloading(val progress: Float) : DownloadState()

    data class Downloaded(val localPath: String) : DownloadState()

    data class Failed(val error: String) : DownloadState()
}
