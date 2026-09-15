package com.example.tijori.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "insights_cache")
data class InsightsCache(
    @PrimaryKey val id: String = "insights_cache_singleton",
    val summary: String,
    val flags: List<String>,
    val generatedAt: Date,
    val periodStart: Date,
    val periodEnd: Date
)