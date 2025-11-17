package com.invoiceapp.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.invoiceapp.util.Resource
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val totalSpendingState by viewModel.totalSpendingState.collectAsState()
    val spendingByCategoryState by viewModel.spendingByCategoryState.collectAsState()

    var selectedPeriod by remember { mutableStateOf(TimePeriod.THIS_MONTH) }

    LaunchedEffect(selectedPeriod) {
        val (startDate, endDate) = selectedPeriod.getDateRange()
        viewModel.loadTotalSpending(startDate, endDate)
        viewModel.loadSpendingByCategory(startDate, endDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Period selector
        Text(
            text = "Analytics",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimePeriod.values().forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { selectedPeriod = period },
                    label = { Text(period.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Total spending card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Total Spending",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                when (val state = totalSpendingState) {
                    is Resource.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    is Resource.Success -> {
                        Text(
                            text = formatCurrency(state.data ?: BigDecimal.ZERO),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    is Resource.Error -> {
                        Text(
                            text = "Error loading data",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    null -> {
                        Text(
                            text = "$0.00",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedPeriod.getDateRangeText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Spending by category
        Text(
            text = "Spending by Category",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        when (val state = spendingByCategoryState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val categoryData = state.data ?: emptyMap()
                if (categoryData.isEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No spending data for this period",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val sortedData = categoryData.entries.sortedByDescending { it.value }
                    val total = sortedData.sumOf { it.value }

                    sortedData.forEach { (category, amount) ->
                        CategorySpendingCard(
                            category = category,
                            amount = amount,
                            percentage = if (total > BigDecimal.ZERO) {
                                (amount.divide(total, 4, BigDecimal.ROUND_HALF_UP) * BigDecimal(100)).toFloat()
                            } else 0f
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            is Resource.Error -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error loading category data",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message ?: "Unknown error",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            null -> {}
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun CategorySpendingCard(
    category: String,
    amount: BigDecimal,
    percentage: Float
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(amount),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "${percentage.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatCurrency(amount: BigDecimal): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US).apply {
        currency = Currency.getInstance("USD")
    }
    return format.format(amount)
}

enum class TimePeriod(val label: String) {
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    THIS_YEAR("This Year");

    fun getDateRange(): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        return when (this) {
            THIS_WEEK -> {
                val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                startOfWeek to today
            }
            THIS_MONTH -> {
                val startOfMonth = today.withDayOfMonth(1)
                startOfMonth to today
            }
            LAST_MONTH -> {
                val lastMonth = today.minusMonths(1)
                val startOfLastMonth = lastMonth.withDayOfMonth(1)
                val endOfLastMonth = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())
                startOfLastMonth to endOfLastMonth
            }
            THIS_YEAR -> {
                val startOfYear = today.withDayOfYear(1)
                startOfYear to today
            }
        }
    }

    fun getDateRangeText(): String {
        val (start, end) = getDateRange()
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        return "${start.format(formatter)} - ${end.format(formatter)}"
    }
}
