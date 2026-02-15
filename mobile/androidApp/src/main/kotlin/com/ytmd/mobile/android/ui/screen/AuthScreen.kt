package com.ytmd.mobile.android.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ytmd.mobile.android.webview.AuthWebView

/**
 * Authentication screen with WebView for Google login.
 * After successful login, cookies are extracted and saved to CookieStore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthComplete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Sign In") },
            navigationIcon = {
                IconButton(onClick = onAuthComplete) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        AuthWebView(
            onCookiesExtracted = { cookies ->
                // TODO: Save cookies via AuthRepository
                onAuthComplete()
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
