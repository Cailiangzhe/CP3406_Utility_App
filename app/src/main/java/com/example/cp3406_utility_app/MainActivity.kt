package com.example.cp3406_utility_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cp3406_utility_app.ui.theme.CP3406_Utility_AppTheme
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CP3406_Utility_AppTheme {
                CurrencyTravelHelperApp()
            }
        }
    }
}

private enum class AppTab {
    Currency,
    Settings
}

private val supportedCurrencies = listOf("AUD", "USD", "CNY", "SGD", "EUR", "JPY")

private val currencyLabels = mapOf(
    "AUD" to "Australian dollar",
    "USD" to "US dollar",
    "CNY" to "Chinese yuan",
    "SGD" to "Singapore dollar",
    "EUR" to "Euro",
    "JPY" to "Japanese yen"
)

private val usdValuePerUnit = mapOf(
    "AUD" to 0.66,
    "USD" to 1.00,
    "CNY" to 0.14,
    "SGD" to 0.74,
    "EUR" to 1.08,
    "JPY" to 0.0064
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyTravelHelperApp() {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Currency) }
    var amountText by rememberSaveable { mutableStateOf("100") }
    var baseCurrency by rememberSaveable { mutableStateOf("AUD") }
    var decimalPlaces by rememberSaveable { mutableStateOf(2) }
    var targetCurrencies by remember {
        mutableStateOf(setOf("USD", "CNY", "SGD", "JPY"))
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Currency Travel Helper",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == AppTab.Currency,
                    onClick = { selectedTab = AppTab.Currency },
                    icon = { Text("C") },
                    label = { Text("Currency") }
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.Settings,
                    onClick = { selectedTab = AppTab.Settings },
                    icon = { Text("S") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            AppTab.Currency -> CurrencyScreen(
                amountText = amountText,
                baseCurrency = baseCurrency,
                targetCurrencies = targetCurrencies,
                decimalPlaces = decimalPlaces,
                onAmountChange = { amountText = it.cleanCurrencyInput() },
                onQuickAmountSelected = { amountText = it },
                modifier = Modifier.padding(innerPadding)
            )

            AppTab.Settings -> SettingsScreen(
                baseCurrency = baseCurrency,
                targetCurrencies = targetCurrencies,
                decimalPlaces = decimalPlaces,
                onBaseCurrencyChange = { baseCurrency = it },
                onTargetCurrencyToggle = { currency ->
                    targetCurrencies = if (currency in targetCurrencies) {
                        targetCurrencies - currency
                    } else {
                        targetCurrencies + currency
                    }
                },
                onDecimalPlacesChange = { decimalPlaces = it },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun CurrencyScreen(
    amountText: String,
    baseCurrency: String,
    targetCurrencies: Set<String>,
    decimalPlaces: Int,
    onAmountChange: (String) -> Unit,
    onQuickAmountSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val visibleTargets = targetCurrencies.filter { it != baseCurrency }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Travel budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = onAmountChange,
                    label = { Text("Amount in $baseCurrency") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("50", "100", "500").forEach { quickAmount ->
                        Button(onClick = { onQuickAmountSelected(quickAmount) }) {
                            Text("$baseCurrency $quickAmount")
                        }
                    }
                }
            }
        }

        Text(
            text = "Converted values",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (visibleTargets.isEmpty()) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Select at least one target currency in Settings.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            visibleTargets.forEach { target ->
                CurrencyResultCard(
                    baseCurrency = baseCurrency,
                    targetCurrency = target,
                    amount = amount,
                    decimalPlaces = decimalPlaces
                )
            }
        }

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Rate source",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Demo rates are used in this first version. Retrofit API integration is planned next.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun CurrencyResultCard(
    baseCurrency: String,
    targetCurrency: String,
    amount: Double,
    decimalPlaces: Int,
    modifier: Modifier = Modifier
) {
    val convertedAmount = convertCurrency(amount, baseCurrency, targetCurrency)
    val oneUnitRate = convertCurrency(1.0, baseCurrency, targetCurrency)

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = targetCurrency,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currencyLabels[targetCurrency].orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = formatCurrency(convertedAmount, targetCurrency, decimalPlaces),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "1 $baseCurrency = ${formatNumber(oneUnitRate, decimalPlaces)} $targetCurrency",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    baseCurrency: String,
    targetCurrencies: Set<String>,
    decimalPlaces: Int,
    onBaseCurrencyChange: (String) -> Unit,
    onTargetCurrencyToggle: (String) -> Unit,
    onDecimalPlacesChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SettingsSection(title = "Base currency") {
            CurrencyChipRow(
                selectedCurrencies = setOf(baseCurrency),
                onCurrencyClick = onBaseCurrencyChange
            )
        }

        SettingsSection(title = "Target currencies") {
            CurrencyChipRow(
                selectedCurrencies = targetCurrencies,
                onCurrencyClick = onTargetCurrencyToggle
            )
        }

        SettingsSection(title = "Decimal places") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 2, 4).forEach { option ->
                    FilterChip(
                        selected = decimalPlaces == option,
                        onClick = { onDecimalPlacesChange(option) },
                        label = { Text(option.toString()) }
                    )
                }
            }
        }

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Current setup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Base: $baseCurrency")
                Text("Targets: ${targetCurrencies.sorted().joinToString()}")
                Text("Decimals: $decimalPlaces")
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}

@Composable
private fun CurrencyChipRow(
    selectedCurrencies: Set<String>,
    onCurrencyClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        supportedCurrencies.chunked(3).forEach { rowCurrencies ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowCurrencies.forEach { currency ->
                    FilterChip(
                        selected = currency in selectedCurrencies,
                        onClick = { onCurrencyClick(currency) },
                        label = { Text(currency) }
                    )
                }
            }
        }
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

private fun convertCurrency(
    amount: Double,
    baseCurrency: String,
    targetCurrency: String
): Double {
    val baseUsdValue = usdValuePerUnit[baseCurrency] ?: 1.0
    val targetUsdValue = usdValuePerUnit[targetCurrency] ?: 1.0
    return amount * baseUsdValue / targetUsdValue
}

private fun formatCurrency(
    amount: Double,
    currency: String,
    decimalPlaces: Int
): String = "$currency ${formatNumber(amount, decimalPlaces)}"

private fun formatNumber(amount: Double, decimalPlaces: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.minimumFractionDigits = decimalPlaces
    formatter.maximumFractionDigits = decimalPlaces
    return formatter.format(amount)
}

@Preview(showBackground = true)
@Composable
fun CurrencyTravelHelperPreview() {
    CP3406_Utility_AppTheme {
        CurrencyTravelHelperApp()
    }
}
