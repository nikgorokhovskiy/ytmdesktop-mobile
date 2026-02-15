package com.ytmd.mobile.data.remote

/**
 * Innertube API endpoint constants.
 * Base URL: https://music.youtube.com/youtubei/v1/
 */
object InnertubeEndpoints {
    const val BASE_URL = "https://music.youtube.com/youtubei/v1/"

    const val PLAYER = "player"
    const val BROWSE = "browse"
    const val SEARCH = "search"
    const val NEXT = "next"
    const val SEARCH_SUGGESTIONS = "music/get_search_suggestions"

    // Client context
    const val CLIENT_NAME = "ANDROID_MUSIC"
    const val CLIENT_VERSION = "7.27.52"
    const val API_KEY = "AIzaSyAOghZGza2MQSZkY_zfZ370N-PUdXEo8AI"

    const val USER_AGENT = "com.google.android.apps.youtube.music/7.27.52 (Linux; U; Android 14)"
}
