package com.invoiceapp.data.repository

import com.invoiceapp.data.api.ApiService
import com.invoiceapp.data.api.CreateInvoiceRequest
import com.invoiceapp.data.api.PagedResponse
import com.invoiceapp.data.api.UpdateInvoiceRequest
import com.invoiceapp.model.Invoice
import com.invoiceapp.model.InvoiceStatus
import com.invoiceapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepository @Inject constructor(
    private val apiService: ApiService
) {

    fun getInvoices(
        status: InvoiceStatus? = null,
        categoryId: UUID? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        page: Int = 0,
        size: Int = 20
    ): Flow<Resource<PagedResponse<Invoice>>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getInvoices(status, categoryId, startDate, endDate, page, size)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch invoices"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)

    fun getInvoiceById(id: UUID): Flow<Resource<Invoice>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getInvoiceById(id)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch invoice"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)

    fun createInvoice(request: CreateInvoiceRequest): Flow<Resource<Invoice>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.createInvoice(request)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to create invoice"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)

    fun updateInvoice(id: UUID, request: UpdateInvoiceRequest): Flow<Resource<Invoice>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.updateInvoice(id, request)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update invoice"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)

    fun deleteInvoice(id: UUID): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.deleteInvoice(id)

            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to delete invoice"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)

    fun uploadInvoice(file: File): Flow<Resource<Invoice>> = flow {
        try {
            emit(Resource.Loading())
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestBody)

            val response = apiService.uploadInvoice(multipartBody)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to upload invoice"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)

    fun getTotalSpending(startDate: LocalDate, endDate: LocalDate): Flow<Resource<BigDecimal>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getTotalSpending(startDate, endDate)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch total spending"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)

    fun getSpendingByCategory(startDate: LocalDate, endDate: LocalDate): Flow<Resource<Map<String, BigDecimal>>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getSpendingByCategory(startDate, endDate)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch spending by category"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }.flowOn(Dispatchers.IO)
}
