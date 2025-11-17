package com.invoiceapp.data.api

import com.invoiceapp.data.dto.*
import com.invoiceapp.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

interface ApiService {

    // Authentication
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<Unit>

    // Categories
    @GET("categories")
    suspend fun getCategories(): Response<List<Category>>

    @GET("categories/{id}")
    suspend fun getCategoryById(@Path("id") id: UUID): Response<Category>

    @POST("categories")
    suspend fun createCategory(@Body category: Category): Response<Category>

    @PUT("categories/{id}")
    suspend fun updateCategory(@Path("id") id: UUID, @Body category: Category): Response<Category>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: UUID): Response<Unit>

    // Invoices
    @GET("invoices")
    suspend fun getInvoices(
        @Query("status") status: InvoiceStatus? = null,
        @Query("categoryId") categoryId: UUID? = null,
        @Query("startDate") startDate: LocalDate? = null,
        @Query("endDate") endDate: LocalDate? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PagedResponse<Invoice>>

    @GET("invoices/{id}")
    suspend fun getInvoiceById(@Path("id") id: UUID): Response<Invoice>

    @POST("invoices")
    suspend fun createInvoice(@Body invoice: CreateInvoiceRequest): Response<Invoice>

    @PUT("invoices/{id}")
    suspend fun updateInvoice(@Path("id") id: UUID, @Body invoice: UpdateInvoiceRequest): Response<Invoice>

    @DELETE("invoices/{id}")
    suspend fun deleteInvoice(@Path("id") id: UUID): Response<Unit>

    @Multipart
    @POST("invoices/upload")
    suspend fun uploadInvoice(@Part file: MultipartBody.Part): Response<Invoice>

    @GET("invoices/analytics/total")
    suspend fun getTotalSpending(
        @Query("startDate") startDate: LocalDate,
        @Query("endDate") endDate: LocalDate
    ): Response<BigDecimal>

    @GET("invoices/analytics/by-category")
    suspend fun getSpendingByCategory(
        @Query("startDate") startDate: LocalDate,
        @Query("endDate") endDate: LocalDate
    ): Response<Map<String, BigDecimal>>

    // Transactions
    @GET("transactions")
    suspend fun getTransactions(
        @Query("startDate") startDate: LocalDate? = null,
        @Query("endDate") endDate: LocalDate? = null,
        @Query("categoryId") categoryId: UUID? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PagedResponse<Transaction>>

    @GET("transactions/{id}")
    suspend fun getTransactionById(@Path("id") id: UUID): Response<Transaction>

    @POST("transactions")
    suspend fun createTransaction(@Body transaction: Transaction): Response<Transaction>

    @PUT("transactions/{id}")
    suspend fun updateTransaction(@Path("id") id: UUID, @Body transaction: Transaction): Response<Transaction>

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: UUID): Response<Unit>

    @Multipart
    @POST("transactions/import/csv")
    suspend fun importTransactionsFromCsv(@Part file: MultipartBody.Part): Response<ImportResult>

    // MCP Queries
    @POST("mcp/query")
    suspend fun queryMcp(@Body request: McpQueryRequest): Response<McpQueryResponse>

    @GET("mcp/examples")
    suspend fun getMcpExamples(): Response<List<String>>
}

data class PagedResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val last: Boolean
)

data class CreateInvoiceRequest(
    val vendorName: String,
    val date: LocalDate,
    val totalAmount: BigDecimal,
    val currency: String = "USD",
    val categoryId: UUID? = null,
    val notes: String? = null,
    val lineItems: List<LineItemRequest> = emptyList(),
    val tags: List<String> = emptyList()
)

data class LineItemRequest(
    val description: String,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val categoryId: UUID? = null
)

data class UpdateInvoiceRequest(
    val vendorName: String?,
    val date: LocalDate?,
    val totalAmount: BigDecimal?,
    val categoryId: UUID?,
    val notes: String?,
    val status: InvoiceStatus?,
    val tags: List<String>?
)

data class ImportResult(
    val batchId: UUID,
    val status: String,
    val successfulRecords: Int,
    val failedRecords: Int,
    val errors: List<String>
)

data class McpQueryRequest(
    val query: String
)

data class McpQueryResponse(
    val query: String,
    val answer: String,
    val intent: String,
    val data: Map<String, Any>,
    val suggestions: List<String>
)
