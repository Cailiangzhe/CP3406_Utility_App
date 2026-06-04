package com.example.cp3406_utility_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cp3406_utility_app.data.HistoricalRatePoint
import com.example.cp3406_utility_app.ui.CurrencyUiState
import com.example.cp3406_utility_app.ui.CurrencyViewModel
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

private val supportedCurrencies = listOf(
    "AUD",
    "USD",
    "CNY",
    "SGD",
    "EUR",
    "JPY",
    "GBP",
    "CAD",
    "NZD",
    "HKD",
    "KRW",
    "THB",
    "MYR",
    "IDR",
    "INR",
    "PHP"
)

private val currencyLabels = mapOf(
    "AUD" to "Australian dollar",
    "USD" to "US dollar",
    "CNY" to "Chinese yuan",
    "SGD" to "Singapore dollar",
    "EUR" to "Euro",
    "JPY" to "Japanese yen",
    "GBP" to "British pound",
    "CAD" to "Canadian dollar",
    "NZD" to "New Zealand dollar",
    "HKD" to "Hong Kong dollar",
    "KRW" to "South Korean won",
    "THB" to "Thai baht",
    "MYR" to "Malaysian ringgit",
    "IDR" to "Indonesian rupiah",
    "INR" to "Indian rupee",
    "PHP" to "Philippine peso"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyTravelHelperApp() {
    val currencyViewModel: CurrencyViewModel = viewModel(factory = CurrencyViewModel.Factory)
    val uiState by currencyViewModel.uiState.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Currency) }

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
                uiState = uiState,
                onAmountChange = currencyViewModel::updateAmount,
                onBaseCurrencyChange = currencyViewModel::updateBaseCurrency,
                onQuickAmountSelected = currencyViewModel::updateAmount,
                onChartCurrencyChange = currencyViewModel::updateChartCurrency,
                onRefreshRates = currencyViewModel::refreshRates,
                modifier = Modifier.padding(innerPadding)
            )

            AppTab.Settings -> SettingsScreen(
                uiState = uiState,
                onBaseCurrencyChange = currencyViewModel::updateBaseCurrency,
                onTargetCurrencyToggle = currencyViewModel::toggleTargetCurrency,
                onDecimalPlacesChange = currencyViewModel::updateDecimalPlaces,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun CurrencyScreen(
    uiState: CurrencyUiState,
    onAmountChange: (String) -> Unit,
    onBaseCurrencyChange: (String) -> Unit,
    onQuickAmountSelected: (String) -> Unit,
    onChartCurrencyChange: (String) -> Unit,
    onRefreshRates: () -> Unit,
    modifier: Modifier = Modifier
) {
    val amount = uiState.amountText.toDoubleOrNull() ?: 0.0
    val visibleTargets = uiState.visibleTargetCurrencies

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
                Text(
                    text = "From currency",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                CurrencyPickerRow(
                    selectedCurrency = uiState.baseCurrency,
                    onCurrencyClick = onBaseCurrencyChange
                )
                OutlinedTextField(
                    value = uiState.amountText,
                    onValueChange = onAmountChange,
                    label = { Text("Amount in ${uiState.baseCurrency}") },
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
                            Text("${uiState.baseCurrency} $quickAmount")
                        }
                    }
                }
            }
        }

        ExchangeRateTrendCard(
            uiState = uiState,
            onChartCurrencyChange = onChartCurrencyChange
        )

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
                    baseCurrency = uiState.baseCurrency,
                    targetCurrency = target,
                    amount = amount,
                    decimalPlaces = uiState.decimalPlaces,
                    rate = uiState.rates[target],
                    isLoading = uiState.isLoading
                )
            }
        }

        RateSourceCard(
            uiState = uiState,
            onRefreshRates = onRefreshRates
        )
    }
}

@Composable
private fun ExchangeRateTrendCard(
    uiState: CurrencyUiState,
    onChartCurrencyChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleTargets = uiState.visibleTargetCurrencies
    val chartCurrency = uiState.chartCurrency
    val points = uiState.chartPoints
    val latestPoint = points.lastOrNull()
    val minRate = points.minOfOrNull { it.rate }
    val maxRate = points.maxOfOrNull { it.rate }

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "30-day exchange trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (chartCurrency.isNotBlank()) {
                            "1 ${uiState.baseCurrency} to $chartCurrency"
                        } else {
                            "Select a target currency to view history"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (visibleTargets.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    visibleTargets.forEach { currency ->
                        FilterChip(
                            selected = chartCurrency == currency,
                            onClick = { onChartCurrencyChange(currency) },
                            label = { Text(currency) }
                        )
                    }
                }
            }

            when {
                uiState.isChartLoading -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(18.dp),
                            strokeWidth = 2.dp
                        )
                        Text("Loading rate history...")
                    }
                }

                uiState.chartErrorMessage != null -> {
                    Text(
                        text = "Chart error: ${uiState.chartErrorMessage}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                points.size < 2 -> {
                    Text(
                        text = "Not enough historical data for this currency pair yet.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                else -> {
                    RateLineChart(
                        points = points,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = points.first().date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = points.last().date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (latestPoint != null && minRate != null && maxRate != null) {
                        Text(
                            text = "Latest: ${formatNumber(latestPoint.rate, uiState.decimalPlaces)} $chartCurrency | Low: ${
                                formatNumber(minRate, uiState.decimalPlaces)
                            } | High: ${formatNumber(maxRate, uiState.decimalPlaces)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RateLineChart(
    points: List<HistoricalRatePoint>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val pointColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier = modifier) {
        if (points.size < 2 || size.width <= 0f || size.height <= 0f) {
            return@Canvas
        }

        val rates = points.map { it.rate }
        val minRate = rates.minOrNull() ?: return@Canvas
        val maxRate = rates.maxOrNull() ?: return@Canvas
        val rateRange = (maxRate - minRate).takeIf { it > 0.0 } ?: 1.0
        val horizontalPadding = 8.dp.toPx()
        val verticalPadding = 12.dp.toPx()
        val chartWidth = (size.width - horizontalPadding * 2).coerceAtLeast(1f)
        val chartHeight = (size.height - verticalPadding * 2).coerceAtLeast(1f)

        repeat(3) { index ->
            val y = verticalPadding + chartHeight * index / 2f
            drawLine(
                color = gridColor,
                start = Offset(horizontalPadding, y),
                end = Offset(size.width - horizontalPadding, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val path = Path()
        val coordinates = points.mapIndexed { index, point ->
            val x = horizontalPadding + chartWidth * index / (points.lastIndex).coerceAtLeast(1)
            val y = verticalPadding + ((maxRate - point.rate) / rateRange).toFloat() * chartHeight
            Offset(x, y)
        }

        coordinates.forEachIndexed { index, offset ->
            if (index == 0) {
                path.moveTo(offset.x, offset.y)
            } else {
                path.lineTo(offset.x, offset.y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        coordinates.lastOrNull()?.let { lastPoint ->
            drawCircle(
                color = pointColor,
                radius = 4.dp.toPx(),
                center = lastPoint
            )
        }
    }
}

@Composable
private fun CurrencyResultCard(
    baseCurrency: String,
    targetCurrency: String,
    amount: Double,
    decimalPlaces: Int,
    rate: Double?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
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
                if (rate != null) {
                    Text(
                        text = formatCurrency(amount * rate, targetCurrency, decimalPlaces),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = if (isLoading) "Loading" else "Unavailable",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                text = if (rate != null) {
                    "1 $baseCurrency = ${formatNumber(rate, decimalPlaces)} $targetCurrency"
                } else {
                    "Rate not available for this currency pair"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RateSourceCard(
    uiState: CurrencyUiState,
    onRefreshRates: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Live rate source",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Exchange rates are loaded from the Frankfurter API.",
                style = MaterialTheme.typography.bodyMedium
            )
            if (uiState.rateDate.isNotBlank()) {
                Text(
                    text = "Latest available date: ${uiState.rateDate}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (uiState.isLoading) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.width(18.dp), strokeWidth = 2.dp)
                    Text("Refreshing rates...")
                }
            }
            uiState.errorMessage?.let { message ->
                Text(
                    text = "Error: $message",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(onClick = onRefreshRates) {
                Text("Refresh rates")
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    uiState: CurrencyUiState,
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
                selectedCurrencies = setOf(uiState.baseCurrency),
                onCurrencyClick = onBaseCurrencyChange
            )
        }

        SettingsSection(title = "Target currencies") {
            CurrencyChipRow(
                selectedCurrencies = uiState.targetCurrencies,
                onCurrencyClick = onTargetCurrencyToggle
            )
        }

        SettingsSection(title = "Decimal places") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 2, 4).forEach { option ->
                    FilterChip(
                        selected = uiState.decimalPlaces == option,
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
                Text("Base: ${uiState.baseCurrency}")
                Text("Targets: ${uiState.targetCurrencies.displayText()}")
                Text("Decimals: ${uiState.decimalPlaces}")
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
private fun CurrencyPickerRow(
    selectedCurrency: String,
    onCurrencyClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        supportedCurrencies.forEach { currency ->
            FilterChip(
                selected = selectedCurrency == currency,
                onClick = { onCurrencyClick(currency) },
                label = { Text(currency) }
            )
        }
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

private fun Set<String>.displayText(): String =
    if (isEmpty()) "None" else sorted().joinToString()

@Preview(showBackground = true)
@Composable
fun CurrencyTravelHelperPreview() {
    CP3406_Utility_AppTheme {
        CurrencyTravelHelperApp()
    }
}
