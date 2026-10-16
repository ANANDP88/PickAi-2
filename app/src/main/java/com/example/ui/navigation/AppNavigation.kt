package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.viewmodel.MainViewModel

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object LiveVoice : Screen("voice")
    object Voice : Screen("voice")
    object CreativeTools : Screen("creative") {
        fun createRoute(tab: Int = 0): String = "creative?tab=$tab"
    }
    object Favorites : Screen("favorites")
    object Settings : Screen("settings")
    object Category : Screen("category/{categoryId}") {
        fun createRoute(categoryId: String): String = "category/$categoryId"
    }
    object ToolDetail : Screen("tool/{toolId}") {
        fun createRoute(toolId: String): String = "tool/$toolId"
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Full 5-tab BottomNavigationBar: Home, Live Voice, Studio, Saved, Settings
 */
@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomNavItems = listOf(
        BottomNavItem(
            route = Screen.Home.route,
            label = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            route = Screen.LiveVoice.route,
            label = "Live Voice",
            selectedIcon = Icons.Filled.Mic,
            unselectedIcon = Icons.Outlined.MicNone
        ),
        BottomNavItem(
            route = Screen.CreativeTools.route,
            label = "Studio",
            selectedIcon = Icons.Filled.AutoAwesome,
            unselectedIcon = Icons.Outlined.AutoAwesome
        ),
        BottomNavItem(
            route = Screen.Favorites.route,
            label = "Saved",
            selectedIcon = Icons.Filled.Bookmark,
            unselectedIcon = Icons.Outlined.BookmarkBorder
        ),
        BottomNavItem(
            route = Screen.Settings.route,
            label = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )

    val isVoiceRoute = currentRoute == Screen.LiveVoice.route

    NavigationBar(
        modifier = modifier.testTag("bottom_navigation_bar"),
        containerColor = if (isVoiceRoute) Color(0xFF090A10) else MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = when {
                item.route == Screen.CreativeTools.route -> currentRoute.startsWith("creative")
                else -> currentRoute == item.route
            }

            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (isVoiceRoute) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary,
                    selectedTextColor = if (isVoiceRoute) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary,
                    unselectedIconColor = if (isVoiceRoute) Color(0xFF64748B) else MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = if (isVoiceRoute) Color(0xFF64748B) else MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = if (isVoiceRoute) Color(0xFF1E2238) else MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag("nav_item_${item.label.lowercase().replace(" ", "_")}")
            )
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    // Show BottomNavigationBar on all main tabs (Home, Voice, Studio, Saved, Settings)
    // Only hide on secondary full detail / category / onboarding screens
    val showBottomBar = currentRoute != Screen.Onboarding.route &&
        !currentRoute.startsWith("tool/") &&
        !currentRoute.startsWith("category/")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController, viewModel = viewModel)
            }

            composable(Screen.LiveVoice.route) {
                VoiceScreen(viewModel = viewModel)
            }

            composable(
                route = "creative?tab={tab}",
                arguments = listOf(
                    navArgument("tab") {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val tab = backStackEntry.arguments?.getInt("tab") ?: 0
                CreativeToolsScreen(viewModel = viewModel, initialTab = tab)
            }

            composable(Screen.CreativeTools.route) {
                CreativeToolsScreen(viewModel = viewModel, initialTab = 0)
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(navController = navController, viewModel = viewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController, viewModel = viewModel)
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(navController = navController, viewModel = viewModel)
            }

            composable(
                route = Screen.Category.route,
                arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                CategoryScreen(
                    categoryId = categoryId,
                    navController = navController,
                    viewModel = viewModel
                )
            }

            composable(
                route = Screen.ToolDetail.route,
                arguments = listOf(navArgument("toolId") { type = NavType.StringType })
            ) { backStackEntry ->
                val toolId = backStackEntry.arguments?.getString("toolId") ?: ""
                ToolDetailScreen(
                    toolId = toolId,
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}
