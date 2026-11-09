package com.streamlite.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.streamlite.app.ui.screens.home.HomeScreen
import com.streamlite.app.ui.screens.library.LibraryScreen
import com.streamlite.app.ui.screens.player.PlayerScreen
import com.streamlite.app.ui.screens.settings.SettingsScreen
import com.streamlite.app.ui.screens.network.NetworkScreen
import com.streamlite.app.ui.screens.image.ImageViewerScreen
import com.streamlite.app.ui.screens.library.FolderScreen
import com.streamlite.app.ui.screens.library.FavoritesScreen
import com.streamlite.app.ui.screens.library.RecentlyPlayedScreen
import com.streamlite.app.ui.screens.splash.SplashScreen
import com.streamlite.app.ui.theme.PremiumBlack
import com.streamlite.app.ui.theme.PremiumWhite

sealed class BottomNavItem(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Home : BottomNavItem(NavRoutes.HOME, "Home", { Icon(Icons.Filled.Home, contentDescription = "Home") })
    object Library : BottomNavItem(NavRoutes.LIBRARY, "Library", { Icon(Icons.Filled.Folder, contentDescription = "Library") })
    object Settings : BottomNavItem(NavRoutes.SETTINGS, "Settings", { Icon(Icons.Filled.Settings, contentDescription = "Settings") })
}

val bottomNavItems = listOf(BottomNavItem.Home, BottomNavItem.Library, BottomNavItem.Settings)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.route == item.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = PremiumBlack,
                    contentColor = PremiumWhite
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = item.icon,
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.SPLASH,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }
        ) {
            composable(NavRoutes.SPLASH) {
                SplashScreen(navController = navController)
            }

            composable(NavRoutes.HOME) {
                HomeScreen(navController = navController)
            }

            composable(NavRoutes.LIBRARY) {
                LibraryScreen(navController = navController)
            }

            composable(NavRoutes.SETTINGS) {
                SettingsScreen()
            }

            composable(
                route = NavRoutes.PLAYER,
                arguments = listOf(navArgument("mediaId") { type = NavType.StringType }),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(400)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400)) }
            ) { backStackEntry ->
                val mediaId = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("mediaId") ?: "",
                    "UTF-8"
                )
                PlayerScreen(mediaId = mediaId, navController = navController)
            }

            composable(
                route = NavRoutes.IMAGE_VIEWER,
                arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val mediaId = backStackEntry.arguments?.getString("mediaId") ?: ""
                ImageViewerScreen(mediaId = mediaId, navController = navController)
            }

            composable(NavRoutes.NETWORK) {
                NetworkScreen(navController = navController)
            }

            composable(
                route = NavRoutes.FOLDER,
                arguments = listOf(navArgument("folderPath") { type = NavType.StringType })
            ) { backStackEntry ->
                val folderPath = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("folderPath") ?: "",
                    "UTF-8"
                )
                FolderScreen(folderPath = folderPath, navController = navController)
            }

            composable(NavRoutes.FAVORITES) {
                FavoritesScreen(navController = navController)
            }

            composable(NavRoutes.RECENTLY_PLAYED) {
                RecentlyPlayedScreen(navController = navController)
            }
        }
    }
}
