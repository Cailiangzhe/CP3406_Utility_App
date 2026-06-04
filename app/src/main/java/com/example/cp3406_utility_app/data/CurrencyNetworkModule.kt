package com.example.cp3406_utility_app.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CurrencyNetworkModule {
    private const val BaseUrl = "https://api.frankfurter.dev/"

    val apiService: FrankfurterApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FrankfurterApiService::class.java)
    }
}
