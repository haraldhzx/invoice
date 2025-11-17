package com.invoiceapp.data.model

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

/**
 * Payer model representing individuals who pay for invoices
 */
@Serializable
data class Payer(
    val id: String,
    val userId: String,
    val name: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val isDefault: Boolean = false,
    val active: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class PayerCreateRequest(
    val name: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val isDefault: Boolean? = null
)

@Serializable
data class PayerUpdateRequest(
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val isDefault: Boolean? = null,
    val active: Boolean? = null
)
