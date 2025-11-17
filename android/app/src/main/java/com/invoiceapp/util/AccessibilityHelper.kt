package com.invoiceapp.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.*

/**
 * Accessibility utilities for Invoice App
 */
object AccessibilityHelper {

    /**
     * Format currency for screen readers
     */
    fun formatCurrencyForAccessibility(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        val formattedAmount = formatter.format(amount)
        return formattedAmount.replace("$", "").let { "$it dollars" }
    }

    /**
     * Format date for screen readers
     */
    fun formatDateForAccessibility(date: String): String {
        return date.replace("-", " ")
    }

    /**
     * Create content description for invoice item
     */
    fun invoiceItemDescription(
        vendorName: String,
        amount: Double,
        date: String,
        category: String?
    ): String {
        val amountText = formatCurrencyForAccessibility(amount)
        val categoryText = category?.let { ", Category: $it" } ?: ""
        return "Invoice from $vendorName, Amount: $amountText, Date: $date$categoryText"
    }

    /**
     * Create content description for budget item
     */
    fun budgetItemDescription(
        name: String,
        spent: Double,
        total: Double,
        percentage: Double
    ): String {
        val spentText = formatCurrencyForAccessibility(spent)
        val totalText = formatCurrencyForAccessibility(total)
        val status = when {
            percentage >= 100 -> "exceeded"
            percentage >= 80 -> "near limit"
            else -> "on track"
        }
        return "Budget: $name, Spent: $spentText of $totalText, ${percentage.toInt()} percent used, $status"
    }

    /**
     * Create content description for category item
     */
    fun categoryItemDescription(
        name: String,
        description: String?,
        isDefault: Boolean
    ): String {
        val descText = description?.let { ", $it" } ?: ""
        val defaultText = if (isDefault) ", Default category" else ""
        return "Category: $name$descText$defaultText"
    }

    /**
     * Scale text for accessibility
     */
    @Composable
    fun scaledTextStyle(baseStyle: TextStyle, scale: Float = 1f): TextStyle {
        return baseStyle.copy(fontSize = (baseStyle.fontSize.value * scale).sp)
    }

    /**
     * Minimum touch target size (48dp as per Material Design)
     */
    const val MinTouchTargetSize = 48
}

/**
 * Extension function to add accessibility semantics to Modifier
 */
fun Modifier.accessibilityLabel(
    label: String,
    role: Role? = null
): Modifier = this.semantics {
    contentDescription = label
    role?.let { this.role = it }
}

/**
 * Extension function for clickable items with accessibility
 */
@Composable
fun Modifier.accessibleClickable(
    label: String,
    role: Role = Role.Button,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this
        .semantics {
            contentDescription = label
            this.role = role
            if (!enabled) {
                disabled()
            }
        }
        .clickable(
            enabled = enabled,
            onClick = onClick,
            interactionSource = interactionSource,
            indication = null
        )
}

/**
 * Extension function for buttons with loading state
 */
fun Modifier.accessibilityState(
    isLoading: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null
): Modifier = this.semantics {
    if (isLoading) {
        stateDescription = "Loading"
    }
    if (isError && errorMessage != null) {
        liveRegion = LiveRegionMode.Assertive
        error(errorMessage)
    }
}

/**
 * Extension function for progress indicators
 */
fun Modifier.accessibilityProgress(
    current: Float,
    total: Float,
    label: String
): Modifier = this.semantics {
    contentDescription = "$label: ${(current / total * 100).toInt()} percent"
    progressBarRangeInfo = ProgressBarRangeInfo(current, 0f..total)
}

/**
 * Extension function for form fields
 */
fun Modifier.accessibilityFormField(
    label: String,
    value: String,
    isError: Boolean = false,
    errorMessage: String? = null
): Modifier = this.semantics {
    contentDescription = "$label: $value"
    if (isError && errorMessage != null) {
        error(errorMessage)
    }
}

/**
 * Extension function for headings
 */
fun Modifier.accessibilityHeading(): Modifier = this.semantics {
    heading()
}

/**
 * Extension function for lists
 */
fun Modifier.accessibilityCollection(
    itemCount: Int,
    collectionType: String = "list"
): Modifier = this.semantics {
    contentDescription = "$collectionType with $itemCount items"
}

/**
 * Extension function for images
 */
fun Modifier.accessibilityImage(
    description: String,
    isDecorative: Boolean = false
): Modifier = this.semantics {
    if (isDecorative) {
        contentDescription = ""
    } else {
        contentDescription = description
        role = Role.Image
    }
}
