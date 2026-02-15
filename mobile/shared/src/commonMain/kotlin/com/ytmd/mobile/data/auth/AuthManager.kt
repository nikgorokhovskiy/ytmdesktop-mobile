package com.ytmd.mobile.data.auth

import com.ytmd.mobile.domain.model.AuthState
import com.ytmd.mobile.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages Google authentication via cookies extracted from WebView login.
 * Constructs SAPISIDHASH header for authenticated Innertube requests.
 */
class AuthManager(
    private val cookieStore: CookieStore,
) : AuthRepository {

    override fun getAuthState(): Flow<AuthState> {
        return cookieStore.getCookies().map { cookies ->
            if (cookies.isNotEmpty() && cookies.containsKey("SAPISID")) {
                AuthState.Authenticated(cookies)
            } else {
                AuthState.Unauthenticated
            }
        }
    }

    override suspend fun saveCookies(cookies: Map<String, String>) {
        cookieStore.saveCookies(cookies)
    }

    override suspend fun clearAuth() {
        cookieStore.clearCookies()
    }

    override suspend fun buildAuthHeaders(): Map<String, String> {
        val sapisid = cookieStore.getCookie("SAPISID") ?: return emptyMap()
        val origin = "https://music.youtube.com"
        val timestamp = currentTimeSeconds()
        val hash = generateSapisidHash(sapisid, origin, timestamp)

        val cookieString = buildCookieString()

        return buildMap {
            put("Authorization", "SAPISIDHASH ${timestamp}_${hash}")
            put("Cookie", cookieString)
            put("Origin", origin)
        }
    }

    private suspend fun buildCookieString(): String {
        val cookies = mutableMapOf<String, String>()
        // Collect current cookies from store
        cookieStore.getCookie("SAPISID")?.let { cookies["SAPISID"] = it }
        cookieStore.getCookie("SID")?.let { cookies["SID"] = it }
        cookieStore.getCookie("__Secure-1PSID")?.let { cookies["__Secure-1PSID"] = it }
        cookieStore.getCookie("__Secure-3PSID")?.let { cookies["__Secure-3PSID"] = it }
        cookieStore.getCookie("HSID")?.let { cookies["HSID"] = it }
        cookieStore.getCookie("SSID")?.let { cookies["SSID"] = it }
        cookieStore.getCookie("APISID")?.let { cookies["APISID"] = it }
        return cookies.entries.joinToString("; ") { "${it.key}=${it.value}" }
    }

    /**
     * Generate SAPISIDHASH for authentication.
     * Format: SHA1(timestamp + " " + SAPISID + " " + origin)
     */
    private fun generateSapisidHash(sapisid: String, origin: String, timestamp: Long): String {
        val input = "$timestamp $sapisid $origin"
        return sha1(input)
    }
}

/**
 * Platform-specific SHA1 implementation (expect function).
 */
expect fun sha1(input: String): String

/**
 * Platform-specific current time in seconds.
 */
expect fun currentTimeSeconds(): Long
