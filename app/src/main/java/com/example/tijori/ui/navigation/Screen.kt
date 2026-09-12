package com.example.tijori.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object StatisticsRoute

@Serializable
data object SettingsRoute

@Serializable
data object AddTransactionRoute

@Serializable
data object ViewAllTransactionsRoute

data class BottomNavItem(
    val route: Any,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(HomeRoute, "Home", Icons.Filled.Home),
    BottomNavItem(StatisticsRoute, "Statistics", Icons.Filled.BarChart),
//    BottomNavItem(SettingsRoute, "Settings", Icons.Filled.Settings)
)

fun NavController.navigateToBottomNavDestination(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavDestination.matchesRoute(route: Any): Boolean {
    return hierarchy.any { it.hasRoute(route::class) }
}