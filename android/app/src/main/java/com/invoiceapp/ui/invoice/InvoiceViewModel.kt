package com.invoiceapp.ui.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invoiceapp.data.api.PagedResponse
import com.invoiceapp.data.repository.InvoiceRepository
import com.invoiceapp.model.Invoice
import com.invoiceapp.model.InvoiceStatus
import com.invoiceapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _invoicesState = MutableStateFlow<Resource<PagedResponse<Invoice>>?>(null)
    val invoicesState: StateFlow<Resource<PagedResponse<Invoice>>?> = _invoicesState.asStateFlow()

    private val _uploadState = MutableStateFlow<Resource<Invoice>?>(null)
    val uploadState: StateFlow<Resource<Invoice>?> = _uploadState.asStateFlow()

    private val _deleteState = MutableStateFlow<Resource<Unit>?>(null)
    val deleteState: StateFlow<Resource<Unit>?> = _deleteState.asStateFlow()

    fun loadInvoices(
        status: InvoiceStatus? = null,
        categoryId: UUID? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        page: Int = 0,
        size: Int = 20
    ) {
        viewModelScope.launch {
            invoiceRepository.getInvoices(status, categoryId, startDate, endDate, page, size).collect { result ->
                _invoicesState.value = result
            }
        }
    }

    fun uploadInvoice(file: File) {
        viewModelScope.launch {
            invoiceRepository.uploadInvoice(file).collect { result ->
                _uploadState.value = result
                if (result is Resource.Success) {
                    // Reload invoices after successful upload
                    loadInvoices()
                }
            }
        }
    }

    fun deleteInvoice(id: UUID) {
        viewModelScope.launch {
            invoiceRepository.deleteInvoice(id).collect { result ->
                _deleteState.value = result
                if (result is Resource.Success) {
                    // Reload invoices after successful delete
                    loadInvoices()
                }
            }
        }
    }

    fun clearUploadState() {
        _uploadState.value = null
    }

    fun clearDeleteState() {
        _deleteState.value = null
    }
}
