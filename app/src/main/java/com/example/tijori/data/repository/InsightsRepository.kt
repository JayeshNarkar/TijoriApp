package com.example.tijori.data.repository

import com.example.tijori.data.dao.InsightsCacheDao
import com.example.tijori.data.dao.TransactionDao
import com.example.tijori.data.entities.InsightsCache
import com.example.tijori.network.ApiInterface
import com.example.tijori.network.InsightsPayloadBuilder
import com.example.tijori.network.InsightsRequest
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

sealed interface InsightsResult {
    data class Success(val cache: InsightsCache) : InsightsResult
    data class Error(val message: String) : InsightsResult
}

class InsightsRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val insightsCacheDao: InsightsCacheDao,
    private val apiInterface: ApiInterface,
    private val payloadBuilder: InsightsPayloadBuilder
) {
    private val appSecret = "TijoriApp"

    suspend fun getInsights(userId: String, currencySymbol: String, forceRefresh: Boolean = false): InsightsResult {
        val (periodStart, periodEnd) = currentMonthBounds()
        val cached = insightsCacheDao.getCached()

        if (!forceRefresh && cached != null && !cached.isStale(periodStart, periodEnd)) {
            return InsightsResult.Success(cached)
        }

        return try {
            val payload = payloadBuilder.build(userId, currencySymbol)
            val response = apiInterface.getInsights(appSecret,
                InsightsRequest(payload.toPromptText())
            )

            if (!response.isSuccessful) {
                return InsightsResult.Error("Server error: ${response.code()}")
            }

            val body = response.body()
            val result = body?.result
                ?: return InsightsResult.Error(body?.message ?: "Empty response")

            val toCache = InsightsCache(
                summary = result.summary,
                flags = result.flags,
                generatedAt = Date(),
                periodStart = periodStart,
                periodEnd = periodEnd
            )
            insightsCacheDao.upsert(toCache)
            InsightsResult.Success(toCache)
        } catch (e: Exception) {
            if (cached != null) InsightsResult.Success(cached)
            else InsightsResult.Error(e.message ?: "Network error")
        }
    }

    private fun currentMonthBounds(): Pair<Date, Date> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val start = cal.time
        val end = Date()
        return start to end
    }
}

private fun InsightsCache.isStale(currentPeriodStart: Date, currentPeriodEnd: Date): Boolean {
    val oneDayMillis = 24 * 60 * 60 * 1000L
    val isOlderThanADay = (Date().time - generatedAt.time) > oneDayMillis
    val isDifferentPeriod = periodStart != currentPeriodStart
    return isOlderThanADay || isDifferentPeriod
}