package com.example.tijori.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.tijori.ui.components.CurrencyPickerDialog
import com.example.tijori.ui.components.EditProfileDialog
import com.example.tijori.ui.components.InsightsEligibilityDialog
import com.example.tijori.ui.components.LogoutConfirmDialog
import com.example.tijori.ui.components.MinimumBalanceDialog
import com.example.tijori.ui.components.ProfileHeader
import com.example.tijori.ui.components.SettingsEntry
import com.example.tijori.ui.components.SettingsSection
import com.example.tijori.ui.components.StartingBalanceDialog
import com.example.tijori.ui.components.ThemeModePickerDialog
import com.example.tijori.ui.components.TijoriTopBar
import com.example.tijori.ui.viewmodel.AppConfigDBViewModel
import com.example.tijori.ui.viewmodel.AppConfigLoadState
import com.example.tijori.ui.viewmodel.TransactionDBViewModel
import com.example.tijori.ui.viewmodel.UserDBViewModel
import com.example.tijori.ui.viewmodel.UserLoadState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.let

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    userViewModel: UserDBViewModel = hiltViewModel(),
    configViewModel: AppConfigDBViewModel = hiltViewModel(),
    transactionViewModel: TransactionDBViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    val userState by userViewModel.currentUserState.collectAsState()
    val user = (userState as? UserLoadState.Loaded)?.user


    val configState by configViewModel.configState.collectAsState()
    val config = (configState as? AppConfigLoadState.Loaded)?.config

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showMinimumBalanceDialog by remember { mutableStateOf(false) }
    var showStartingBalanceDialog by remember { mutableStateOf(false) }

    var showInsightsDialog by remember { mutableStateOf(false) }
    var insightsCount by remember { mutableStateOf(0) }
    var insightsDaySpan by remember { mutableStateOf(0L) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        topBar = { TijoriTopBar(title = "Settings", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            ProfileHeader(
                firstName = user?.firstName ?: "User",
                lastName = user?.lastName,
                profilePictureURI = user?.profilePictureURI
            )

            SettingsSection(
                title = "Your Info", entries = listOf(
                    SettingsEntry(
                        label = "Edit Profile",
                        icon = Icons.Filled.ManageAccounts,
                        onClick = { showEditProfileDialog = true }), SettingsEntry(
                        label = "Notifications",
                        icon = Icons.Filled.Notifications,
                        trailingText = if (config?.enableNotifications == true) "On" else "Off",
                        onClick = {
                            config?.let {
                                val newValue = !it.enableNotifications
                                configViewModel.updateNotificationsEnabled(newValue)
                                Toast.makeText(
                                    context,
                                    "Notifications Turned: ${if (newValue) "On" else "Off"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }),
//                    SettingsEntry(
//                        label = "Privacy & Security", icon = Icons.Filled.Lock
//                    ),
                    SettingsEntry(
                        label = "Logout",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        onClick = { showLogoutDialog = true })
                ), modifier = Modifier.padding(top = 16.dp)
            )

            SettingsSection(
                title = "Transactions", entries = listOf(
//                    SettingsEntry(
//                        label = "Categories", icon = Icons.Filled.Category
//                    ),
                    SettingsEntry(
                        label = "Currency",
                        trailingText = config?.currencyCode ?: "",
                        onClick = { showCurrencyDialog = true },
                        icon = Icons.Filled.CurrencyExchange
                    ),
                    SettingsEntry(
                        label = "Set Minimum Balance",
                        trailingText = config?.minimumBalance?.let { "${config.currencySymbol}${it}" }
                            ?: "Off",
                        onClick = { showMinimumBalanceDialog = true },
                        icon=Icons.Filled.AccountBalance
                        ),
                    SettingsEntry(
                        label = "Starting Balance",
                        trailingText = config?.let { "${it.currencySymbol}${it.startingBalance}" }
                            ?: "",
                        onClick = { showStartingBalanceDialog = true },
                        icon=Icons.Filled.AccountBalanceWallet
                        ),
                ),
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )

            SettingsSection(
                title = "Others", entries = listOf(
                    SettingsEntry(
                        label = "Theme",
                        trailingText = config?.themeMode?.name ?: "",
                        icon = Icons.Filled.DarkMode,
                        onClick = { showThemeDialog = true }),
                    SettingsEntry(
                        label = "AI Insights",
                        icon = Icons.Filled.AutoAwesome,
                        trailingText = if (config?.enableInsights == true) "On" else "Off",
                        onClick = {
                            user?.let { currentUser ->
                                coroutineScope.launch {
                                    val stats = transactionViewModel.getEligibilityStats(currentUser.id)
                                    insightsCount = stats.count
                                    insightsDaySpan = if (stats.oldestDate != null && stats.newestDate != null) {
                                        TimeUnit.MILLISECONDS.toDays(stats.newestDate.time - stats.oldestDate.time)
                                    } else 0L
                                    showInsightsDialog = true
                                }
                            }
                        }
                    )
                )
            )
        }
    }

    if (showInsightsDialog) {
        InsightsEligibilityDialog(
            transactionCount = insightsCount,
            dayRangeSpan = insightsDaySpan,
            currentlyEnabled = config?.enableInsights == true,
            onConfirm = { newValue -> configViewModel.updateEnableInsights(newValue) },
            onDismiss = { showInsightsDialog = false }
        )
    }

    if (showStartingBalanceDialog && config != null) {
        StartingBalanceDialog(
            currentBalance = config.startingBalance,
            currentDate = config.startingBalanceDate,
            onConfirm = { balance, date -> configViewModel.updateStartingBalance(balance, date) },
            onDismiss = { showStartingBalanceDialog = false })
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            email = user?.email ?: "",
            firstName = user?.firstName,
            lastName = user?.lastName,
            onConfirm = { firstName, lastName -> userViewModel.updateProfile(firstName, lastName) },
            onDismiss = { showEditProfileDialog = false })
    }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = { userViewModel.logout() },
            onDismiss = { showLogoutDialog = false })
    }

    if (showCurrencyDialog && config != null) {
        CurrencyPickerDialog(
            currentCode = config.currencyCode,
            onConfirm = { code, symbol -> configViewModel.updateCurrency(code, symbol) },
            onDismiss = { showCurrencyDialog = false })
    }

    if (showThemeDialog && config != null) {
        ThemeModePickerDialog(
            currentMode = config.themeMode,
            onConfirm = { mode -> configViewModel.updateThemeMode(mode) },
            onDismiss = { showThemeDialog = false })
    }

    if (showMinimumBalanceDialog && config != null) {
        MinimumBalanceDialog(
            currentMinimum = config.minimumBalance,
            onConfirm = { minimum -> configViewModel.updateMinimumBalance(minimum) },
            onDismiss = { showMinimumBalanceDialog = false })
    }
}