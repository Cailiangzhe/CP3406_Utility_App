package com.example.cp3406_utility_app.data

import retrofit2.http.GET
import retrofit2.http.Query

interface FrankfurterApiService {
    @GET("v2/rates")
    suspend fun getLatestRates(
        @Query("base") base: String,
        @Query("quotes") quotes: String
    ): List<ExchangeRateDto>

    @GET("v2/rates")
    suspend fun getHistoricalRates(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("base") base: String,
        @Query("quotes") quotes: String
    ): List<ExchangeRateDto>
}

data class ExchangeRateDto(
    val date: String,
    val base: String,
    val quote: String,
    val rate: Double
)
