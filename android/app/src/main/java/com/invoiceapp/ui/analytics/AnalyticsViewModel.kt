package com.invoiceapp.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invoiceapp.data.repository.InvoiceRepository
import com.invoiceapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _totalSpendingState = MutableStateFlow<Resource<BigDecimal>?>(null)
    val totalSpendingState: StateFlow<Resource<BigDecimal>?> = _totalSpendingState.asStateFlow()

    private val _spendingByCategoryState = MutableStateFlow<Resource<Map<String, BigDecimal>>?>(null)
    val spendingByCategoryState: StateFlow<Resource<Map<String, BigDecimal>>?> = _spendingByCategoryState.asStateFlow()

    fun loadTotalSpending(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            invoiceRepository.getTotalSpending(startDate, endDate).collect { result ->
                _totalSpendingState.value = result
            }
        }
    }

    fun loadSpendingByCategory(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            invoiceRepository.getSpendingByCategory(startDate, endDate).collect { result ->
                _spendingByCategoryState.value = result
            }
        }
    }
}
