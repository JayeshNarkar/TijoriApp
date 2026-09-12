package com.example.tijori.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tijori.data.entities.AppConfig
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface AppConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: AppConfig)

    @Update
    suspend fun update(config: AppConfig)

    @Query("SELECT * FROM app_config WHERE id = 'app_config_singleton' LIMIT 1")
    fun getConfig(): Flow<AppConfig?>

    @Query("SELECT * FROM app_config WHERE id = 'app_config_singleton' LIMIT 1")
    suspend fun getConfigOnce(): AppConfig?

    @Query("UPDATE app_config SET startingBalance = :balance, startingBalanceDate = :date WHERE id = 'app_config_singleton'")
    suspend fun updateStartingBalance(balance: Double, date: Date)

    @Query("UPDATE app_config SET currencyCode = :code, currencySymbol = :symbol WHERE id = 'app_config_singleton'")
    suspend fun updateCurrency(code: String, symbol: String)

    @Query("UPDATE app_config SET themeMode = :mode WHERE id = 'app_config_singleton'")
    suspend fun updateThemeMode(mode: com.example.tijori.data.entities.ThemeMode)

    @Query("UPDATE app_config SET weekStartDay = :day WHERE id = 'app_config_singleton'")
    suspend fun updateWeekStartDay(day: com.example.tijori.data.entities.WeekStartDay)

    @Query("UPDATE app_config SET minimumBalance = :minimum WHERE id = 'app_config_singleton'")
    suspend fun updateMinimumBalance(minimum: Double?)

    @Query("UPDATE app_config SET budgetCycleStartDay = :day WHERE id = 'app_config_singleton'")
    suspend fun updateBudgetCycleStartDay(day: Int)

    @Query("UPDATE app_config SET enableNotifications = :enabled WHERE id = 'app_config_singleton'")
    suspend fun updateNotificationsEnabled(enabled: Boolean)
}