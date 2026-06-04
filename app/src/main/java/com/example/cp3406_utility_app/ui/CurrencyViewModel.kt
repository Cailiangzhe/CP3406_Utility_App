package com.example.cp3406_utility_app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cp3406_utility_app.data.CurrencyNetworkModule
import com.example.cp3406_utility_app.data.CurrencyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CurrencyUiState(
    val amountText: String = "100",
    val baseCurrency: String = "AUD",
    val targetCurrencies: Set<String> = setOf("USD", "CNY", "SGD", "JPY", "EUR", "GBP"),
    val decimalPlaces: Int = 2,
    val rates: Map<String, Double> = emptyMap(),
    val rateDate: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
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
        _uiState.update { it.copy(baseCurrency = currency) }
        refreshRates()
    }

    fun toggleTargetCurrency(currency: String) {
        _uiState.update { state ->
            val updatedTargets = if (currency in state.targetCurrencies) {
                state.targetCurrencies - currency
            } else {
                state.targetCurrencies + currency
            }
            state.copy(targetCurrencies = updatedTargets)
        }
        refreshRates()
    }

    fun updateDecimalPlaces(decimalPlaces: Int) {
        _uiState.update { it.copy(decimalPlaces = decimalPlaces) }
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
                        isLoading = false,
                        errorMessage = null
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getLatestRates(
                baseCurrency = currentState.baseCurrency,
                targetCurrencies = targets
            ).fold(
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
