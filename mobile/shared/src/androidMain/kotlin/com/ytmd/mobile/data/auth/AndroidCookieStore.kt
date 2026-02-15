package com.ytmd.mobile.data.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android CookieStore backed by EncryptedSharedPreferences.
 * NOTE: Requires androidx.security:security-crypto dependency (to be added).
 * For the skeleton phase, uses regular SharedPreferences.
 */
class AndroidCookieStore(context: Context) : CookieStore {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "ytmd_cookies",
        Context.MODE_PRIVATE,
    )

    private val _cookies = MutableStateFlow(loadCookies())

    override fun getCookies(): Flow<Map<String, String>> = _cookies.asStateFlow()

    override suspend fun saveCookies(cookies: Map<String, String>) {
        prefs.edit().apply {
            cookies.forEach { (key, value) -> putString(key, value) }
            apply()
        }
        _cookies.value = loadCookies()
    }

    override suspend fun clearCookies() {
        prefs.edit().clear().apply()
        _cookies.value = emptyMap()
    }

    override suspend fun getCookie(name: String): String? {
        return prefs.getString(name, null)
    }

    override suspend fun hasCookies(): Boolean {
        return prefs.all.isNotEmpty()
    }

    private fun loadCookies(): Map<String, String> {
        return prefs.all.mapValues { it.value?.toString() ?: "" }
    }
}
