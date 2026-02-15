package com.ytmd.mobile.android.webview

import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

private const val MUSIC_URL = "https://music.youtube.com"
private const val ACCOUNTS_URL = "https://accounts.google.com"

/**
 * WebView composable for Google account login.
 * Navigates to YouTube Music, intercepts cookies after successful login.
 * Extracts SAPISID, SID, __Secure-1PSID, etc.
 */
@Composable
fun AuthWebView(
    onCookiesExtracted: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): Boolean {
                        return false
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        if (url?.startsWith(MUSIC_URL) == true) {
                            extractCookies(url, onCookiesExtracted)
                        }
                    }
                }

                loadUrl(MUSIC_URL)
            }
        },
        modifier = modifier,
    )
}

private fun extractCookies(
    url: String,
    onCookiesExtracted: (Map<String, String>) -> Unit,
) {
    val cookieManager = CookieManager.getInstance()
    val cookieString = cookieManager.getCookie(url) ?: return
    val cookies = parseCookieString(cookieString)

    // Check if we have the essential cookies for authentication
    if (cookies.containsKey("SAPISID") && cookies.containsKey("SID")) {
        onCookiesExtracted(cookies)
    }
}

private fun parseCookieString(cookieString: String): Map<String, String> {
    return cookieString
        .split(";")
        .map { it.trim() }
        .filter { it.contains("=") }
        .associate { cookie ->
            val (name, value) = cookie.split("=", limit = 2)
            name.trim() to value.trim()
        }
}
