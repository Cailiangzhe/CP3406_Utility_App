package com.example.cp3406_utility_app.data

data class ExchangeRateResult(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)

class CurrencyRepository(
    private val apiService: FrankfurterApiService
) {
    suspend fun getLatestRates(
        baseCurrency: String,
        targetCurrencies: List<String>
    ): Result<ExchangeRateResult> = runCatching {
        val response = apiService.getLatestRates(
            base = baseCurrency,
            quotes = targetCurrencies.joinToString(",")
        )
        val rates = response.associate { it.quote to it.rate }
        ExchangeRateResult(
            base = response.firstOrNull()?.base ?: baseCurrency,
            date = response.firstOrNull()?.date ?: "Latest available",
            rates = rates
        )
    }
}
