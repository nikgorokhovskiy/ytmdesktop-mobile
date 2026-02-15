package com.ytmd.mobile.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ytmd.mobile.android.ui.navigation.AppNavigation
import com.ytmd.mobile.android.ui.theme.YtmdTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YtmdTheme {
                AppNavigation()
            }
        }
    }
}
