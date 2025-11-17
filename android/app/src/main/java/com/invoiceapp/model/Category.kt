package com.invoiceapp.model

import java.time.LocalDateTime
import java.util.UUID

data class Category(
    val id: UUID,
    val name: String,
    val type: CategoryType,
    val icon: String?,
    val color: String?,
    val description: String?,
    val parentId: UUID?,
    val isCustom: Boolean,
    val userId: UUID?,
    val createdAt: LocalDateTime
)

enum class CategoryType {
    EXPENSE,
    INCOME
}
