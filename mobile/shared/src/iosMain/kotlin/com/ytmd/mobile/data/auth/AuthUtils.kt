package com.ytmd.mobile.data.auth

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA1
import platform.CoreCrypto.CC_SHA1_DIGEST_LENGTH
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * iOS actual implementations for auth utility functions.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun sha1(input: String): String {
    val data = input.encodeToByteArray()
    val digest = UByteArray(CC_SHA1_DIGEST_LENGTH)

    data.usePinned { pinned ->
        digest.usePinned { digestPinned ->
            CC_SHA1(pinned.addressOf(0), data.size.convert(), digestPinned.addressOf(0))
        }
    }

    return digest.joinToString("") { it.toString(16).padStart(2, '0') }
}

actual fun currentTimeSeconds(): Long {
    return NSDate().timeIntervalSince1970.toLong()
}
