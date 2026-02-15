package com.ytmd.mobile.android.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ytmd.mobile.android.ui.component.MiniPlayer
import com.ytmd.mobile.android.ui.screen.AuthScreen
import com.ytmd.mobile.android.ui.screen.HomeScreen
import com.ytmd.mobile.android.ui.screen.PlayerScreen

object Routes {
    const val HOME = "home"
    const val PLAYER = "player"
    const val AUTH = "auth"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            MiniPlayer(
                onExpand = { navController.navigate(Routes.PLAYER) },
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        onNavigateToAuth = { navController.navigate(Routes.AUTH) },
                        onNavigateToPlayer = { navController.navigate(Routes.PLAYER) },
                    )
                }
                composable(Routes.PLAYER) {
                    PlayerScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.AUTH) {
                    AuthScreen(
                        onAuthComplete = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
