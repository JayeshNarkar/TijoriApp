package com.example.tijori.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tijori.data.entities.InsightsCache

@Dao
interface InsightsCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(cache: InsightsCache)

    @Query("SELECT * FROM insights_cache WHERE id = 'insights_cache_singleton' LIMIT 1")
    suspend fun getCached(): InsightsCache?
}