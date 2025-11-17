package com.invoiceapp.model

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val role: UserRole,
    val createdAt: LocalDateTime,
    val lastLoginAt: LocalDateTime? = null
)

enum class UserRole {
    USER,
    ADMIN
}
