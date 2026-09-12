package com.example.tijori.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tijori.ui.components.BottomBar
import com.example.tijori.ui.navigation.AddTransactionRoute
import com.example.tijori.ui.navigation.ViewAllTransactionsRoute
import com.example.tijori.ui.navigation.HomeRoute
import com.example.tijori.ui.navigation.SettingsRoute
import com.example.tijori.ui.navigation.StatisticsRoute
import com.example.tijori.ui.navigation.bottomNavItems
import com.example.tijori.ui.navigation.matchesRoute
import com.example.tijori.ui.navigation.navigateToBottomNavDestination

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.matchesRoute(item.route) == true
    }
    val context = LocalContext.current
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        // TODO: if !allGranted, beg
    }

    LaunchedEffect(Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_SMS
                ) == PackageManager.PERMISSION_GRANTED

        if (!alreadyGranted) {
            smsPermissionLauncher.launch(
                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
            )
        }
    }
    Scaffold(
        modifier = modifier, bottomBar = {
            if (showBottomBar) {
                BottomBar(
                    currentDestination = currentDestination,
                    onTabClick = { route -> navController.navigateToBottomNavDestination(route) },
                    onAddExpenseClick = {
                        navController.navigateToBottomNavDestination(
                            AddTransactionRoute
                        )
                    })
            }
        }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    onNavigateToSettings = {
                        navController.navigateToBottomNavDestination(SettingsRoute)
                    },onNavigateToViewAllTransactions = {
                        navController.navigateToBottomNavDestination(ViewAllTransactionsRoute)
                    })
            }
            composable<StatisticsRoute> { StatisticsScreen() }
            composable<SettingsRoute> {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable<AddTransactionRoute> {
                AddTransactionScreen(onBack = { navController.popBackStack() })
            }

            composable<ViewAllTransactionsRoute> {
                ViewAllTransactionsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}