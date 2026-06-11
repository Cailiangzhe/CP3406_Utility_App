package com.example.cp3406_utility_app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cp3406_utility_app.data.CurrencyNetworkModule
import com.example.cp3406_utility_app.data.CurrencyRepository
import com.example.cp3406_utility_app.data.HistoricalRatePoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppThemeMode(val label: String) {
    System("System"),
    Light("Light"),
    Dark("Dark")
}

data class CurrencyUiState(
    val amountText: String = "100",
    val baseCurrency: String = "AUD",
    val targetCurrencies: Set<String> = setOf("USD", "CNY", "SGD", "JPY", "EUR", "GBP"),
    val decimalPlaces: Int = 2,
    val rates: Map<String, Double> = emptyMap(),
    val rateDate: String = "",
    val chartCurrency: String = "USD",
    val chartRangeDays: Int = 30,
    val chartPoints: List<HistoricalRatePoint> = emptyList(),
    val isLoading: Boolean = false,
    val isChartLoading: Boolean = false,
    val chartErrorMessage: String? = null,
    val errorMessage: String? = null,
    val themeMode: AppThemeMode = AppThemeMode.System
) {
    val visibleTargetCurrencies: List<String>
        get() = targetCurrencies.filter { it != baseCurrency }.sorted()
}

class CurrencyViewModel(
    private val repository: CurrencyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CurrencyUiState())
    val uiState: StateFlow<CurrencyUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null

    init {
        refreshRates()
    }

    fun updateAmount(amountText: String) {
        _uiState.update { it.copy(amountText = amountText.cleanCurrencyInput()) }
    }

    fun updateBaseCurrency(currency: String) {
        _uiState.update { state ->
            val nextChartCurrency = state.chartCurrency.takeIf {
                it != currency && it in state.targetCurrencies
            } ?: state.targetCurrencies.firstOrNull { it != currency } ?: ""
            state.copy(baseCurrency = currency, chartCurrency = nextChartCurrency)
        }
        refreshRates()
    }

    fun toggleTargetCurrency(currency: String) {
        _uiState.update { state ->
            val updatedTargets = if (currency in state.targetCurrencies) {
                state.targetCurrencies - currency
            } else {
                state.targetCurrencies + currency
            }
            val visibleTargets = updatedTargets.filter { it != state.baseCurrency }.sorted()
            val nextChartCurrency = state.chartCurrency.takeIf { it in visibleTargets }
                ?: visibleTargets.firstOrNull()
                ?: ""
            state.copy(targetCurrencies = updatedTargets, chartCurrency = nextChartCurrency)
        }
        refreshRates()
    }

    fun updateDecimalPlaces(decimalPlaces: Int) {
        _uiState.update { it.copy(decimalPlaces = decimalPlaces) }
    }

    fun updateThemeMode(themeMode: AppThemeMode) {
        _uiState.update { it.copy(themeMode = themeMode) }
    }

    fun updateChartCurrency(currency: String) {
        _uiState.update { it.copy(chartCurrency = currency) }
        refreshRates()
    }

    fun updateChartRange(days: Int) {
        _uiState.update { it.copy(chartRangeDays = days) }
        refreshRates()
    }

    fun refreshRates() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val currentState = _uiState.value
            val targets = currentState.visibleTargetCurrencies

            if (targets.isEmpty()) {
                _uiState.update {
                    it.copy(
                        rates = emptyMap(),
                        rateDate = "",
                        chartCurrency = "",
                        chartPoints = emptyList(),
                        isLoading = false,
                        isChartLoading = false,
                        chartErrorMessage = null,
                        errorMessage = null
                    )
                }
                return@launch
            }

            val chartCurrency = currentState.chartCurrency.takeIf { it in targets } ?: targets.first()
            val fromDate = formattedDateDaysAgo(currentState.chartRangeDays)
            val toDate = formattedDateDaysAgo(0)

            _uiState.update {
                it.copy(
                    chartCurrency = chartCurrency,
                    isLoading = true,
                    isChartLoading = true,
                    errorMessage = null,
                    chartErrorMessage = null
                )
            }

            val latestRates = async {
                repository.getLatestRates(
                    baseCurrency = currentState.baseCurrency,
                    targetCurrencies = targets
                )
            }
            val historicalRates = async {
                repository.getHistoricalRates(
                    baseCurrency = currentState.baseCurrency,
                    targetCurrency = chartCurrency,
                    fromDate = fromDate,
                    toDate = toDate
                )
            }

            latestRates.await().fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            rates = result.rates,
                            rateDate = result.date,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            rates = emptyMap(),
                            isLoading = false,
                            errorMessage = error.message ?: "Unable to load exchange rates."
                        )
                    }
                }
            )

            historicalRates.await().fold(
                onSuccess = { points ->
                    _uiState.update {
                        it.copy(
                            chartPoints = points,
                            isChartLoading = false,
                            chartErrorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            chartPoints = emptyList(),
                            isChartLoading = false,
                            chartErrorMessage = error.message ?: "Unable to load rate history."
                        )
                    }
                }
            )
        }
    }

    private fun String.cleanCurrencyInput(): String {
        val filtered = filter { it.isDigit() || it == '.' }
        val firstDecimal = filtered.indexOf('.')
        return if (firstDecimal == -1) {
            filtered
        } else {
            filtered.take(firstDecimal + 1) +
                filtered.drop(firstDecimal + 1).replace(".", "")
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = CurrencyRepository(CurrencyNetworkModule.apiService)
                return CurrencyViewModel(repository) as T
            }
        }
    }
}

private fun formattedDateDaysAgo(daysAgo: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
}
