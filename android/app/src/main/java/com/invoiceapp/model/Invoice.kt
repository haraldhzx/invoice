package com.invoiceapp.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Invoice(
    val id: UUID,
    val userId: UUID,
    val vendorName: String?,
    val date: LocalDate?,
    val totalAmount: BigDecimal,
    val currency: String,
    val status: InvoiceStatus,
    val confidence: BigDecimal?,
    val notes: String?,
    val categoryId: UUID?,
    val category: Category?,
    val lineItems: List<LineItem> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val tags: List<String> = emptyList(),
    val extractedData: Map<String, Any>? = null,
    val processedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class InvoiceStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    REVIEW_REQUIRED,
    REJECTED
}

data class LineItem(
    val id: UUID,
    val description: String,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val categoryId: UUID?,
    val category: Category?
)

data class Attachment(
    val id: UUID,
    val fileName: String,
    val fileType: String,
    val fileSize: Long,
    val storageKey: String,
    val url: String?,
    val uploadedAt: LocalDateTime
)
