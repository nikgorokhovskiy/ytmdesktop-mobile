package com.ytmd.mobile.data.auth

import java.security.MessageDigest

/**
 * Android actual implementations for auth utility functions.
 */
actual fun sha1(input: String): String {
    val md = MessageDigest.getInstance("SHA-1")
    val digest = md.digest(input.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
}

actual fun currentTimeSeconds(): Long {
    return System.currentTimeMillis() / 1000
}
