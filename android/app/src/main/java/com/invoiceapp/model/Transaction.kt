package com.invoiceapp.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Transaction(
    val id: UUID,
    val userId: UUID,
    val transactionDate: LocalDate,
    val description: String,
    val amount: BigDecimal,
    val type: TransactionType,
    val categoryId: UUID?,
    val category: Category?,
    val merchantName: String?,
    val accountName: String?,
    val referenceNumber: String?,
    val notes: String?,
    val tags: List<String> = emptyList(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class TransactionType {
    DEBIT,
    CREDIT
}
