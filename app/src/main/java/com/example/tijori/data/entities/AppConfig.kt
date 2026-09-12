package com.example.tijori.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.TimeZone

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey
    val id: String = "app_config_singleton",
    val currencyCode: String = "USD",
    val currencySymbol: String = "$",
    val timezoneId: String = TimeZone.getDefault().id,
    val weekStartDay: WeekStartDay = WeekStartDay.MONDAY,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val budgetCycleStartDay: Int = 1,  // 1-28
    val minimumBalance: Double? = null,
    val enableNotifications: Boolean = true,
    val startingBalance: Double = 0.0,
    val startingBalanceDate: Date = Date(),
)

enum class WeekStartDay {
    MONDAY,
    SUNDAY,
    SATURDAY
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

//enum class BackupFrequency {
//    DAILY,
//    WEEKLY,
//    MONTHLY,
//    NEVER
//}
//
//enum class SyncFrequency {
//    MANUAL,
//    HOURLY,
//    DAILY,
//    WEEKLY
//}