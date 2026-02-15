package com.ytmd.mobile.data.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

/**
 * iOS CookieStore backed by NSUserDefaults.
 * In production, should use Keychain for secure storage.
 */
class IosCookieStore : CookieStore {

    private val defaults = NSUserDefaults.standardUserDefaults
    private val storageKey = "ytmd_cookies"

    private val _cookies = MutableStateFlow(loadCookies())

    override fun getCookies(): Flow<Map<String, String>> = _cookies.asStateFlow()

    override suspend fun saveCookies(cookies: Map<String, String>) {
        val existing = loadCookies().toMutableMap()
        existing.putAll(cookies)
        defaults.setObject(existing as Any, forKey = storageKey)
        defaults.synchronize()
        _cookies.value = existing
    }

    override suspend fun clearCookies() {
        defaults.removeObjectForKey(storageKey)
        defaults.synchronize()
        _cookies.value = emptyMap()
    }

    override suspend fun getCookie(name: String): String? {
        return loadCookies()[name]
    }

    override suspend fun hasCookies(): Boolean {
        return loadCookies().isNotEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadCookies(): Map<String, String> {
        val dict = defaults.dictionaryForKey(storageKey) ?: return emptyMap()
        return dict.mapKeys { it.key as String }.mapValues { it.value as String }
    }
}
