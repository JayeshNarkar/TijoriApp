package com.example.tijori.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tijori.data.dao.AppConfigDao
import com.example.tijori.data.entities.AppConfig
import com.example.tijori.data.entities.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

sealed interface AppConfigLoadState {
    data object Loading : AppConfigLoadState
    data class Loaded(val config: AppConfig) : AppConfigLoadState
}

@HiltViewModel
class AppConfigDBViewModel @Inject constructor(
    private val appConfigDao: AppConfigDao
) : ViewModel() {

    val configState: StateFlow<AppConfigLoadState> = appConfigDao.getConfig()
        .map { config ->
            if (config != null) {
                AppConfigLoadState.Loaded(config) as AppConfigLoadState
            } else {
                AppConfigLoadState.Loading
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppConfigLoadState.Loading)

    init {
        // Seed the singleton row on first-ever launch, since there's no
        // sign-up flow that creates it the way addUser() does for Users.
        viewModelScope.launch {
            if (appConfigDao.getConfigOnce() == null) {
                appConfigDao.upsert(AppConfig())
            }
        }
    }

    fun updateCurrency(code: String, symbol: String) {
        viewModelScope.launch {
            appConfigDao.updateCurrency(code, symbol)
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            appConfigDao.updateThemeMode(mode)
        }
    }

    fun updateStartingBalance(balance: Double, date: Date) {
        viewModelScope.launch {
            appConfigDao.updateStartingBalance(balance, date)
        }
    }

    fun updateMinimumBalance(minimum: Double?) {
        viewModelScope.launch {
            appConfigDao.updateMinimumBalance(minimum)
        }
    }

    fun updateEnableInsights(enabled: Boolean) {
        viewModelScope.launch {
            appConfigDao.updateEnableInsights(enabled)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appConfigDao.updateNotificationsEnabled(enabled)
        }
    }
}