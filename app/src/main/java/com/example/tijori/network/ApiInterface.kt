package com.example.tijori.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiInterface {
    @POST("insights")
    suspend fun getInsights(
        @Header("X-App-Secret") secret: String,
        @Body request: InsightsRequest
    ): Response<InsightsResponse>
}