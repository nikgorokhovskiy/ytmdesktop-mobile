package com.ytmd.mobile.data.auth

import kotlinx.coroutines.flow.Flow

/**
 * Persistent cookie storage for Google authentication.
 * Platform-specific implementations use secure storage (EncryptedSharedPreferences / Keychain).
 */
interface CookieStore {

    fun getCookies(): Flow<Map<String, String>>

    suspend fun saveCookies(cookies: Map<String, String>)

    suspend fun clearCookies()

    suspend fun getCookie(name: String): String?

    suspend fun hasCookies(): Boolean
}
