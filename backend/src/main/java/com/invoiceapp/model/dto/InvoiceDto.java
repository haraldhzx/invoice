package com.invoiceapp.model.dto;

import com.invoiceapp.model.enums.InvoiceStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {
    private UUID id;

    @Size(max = 255, message = "Vendor name must not exceed 255 characters")
    private String vendorName;

    @Size(max = 100, message = "Invoice number must not exceed 100 characters")
    private String invoiceNumber;

    @NotNull(message = "Invoice date is required")
    private LocalDate date;

    private LocalDate dueDate;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal totalAmount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
    private String currency;

    @DecimalMin(value = "0.0", message = "Tax amount must be non-negative")
    private BigDecimal taxAmount;

    private UUID categoryId;
    private String categoryName;

    private UUID subcategoryId;
    private String subcategoryName;

    private InvoiceStatus status;

    @DecimalMin(value = "0.0", message = "Confidence must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Confidence must be between 0 and 1")
    private BigDecimal confidence;

    private Map<String, Object> extractedData;

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;

    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

    private Boolean isRecurring;

    @Size(max = 20, message = "Recurring frequency must not exceed 20 characters")
    private String recurringFrequency;

    @Builder.Default
    private List<AttachmentDto> attachments = new ArrayList<>();

    @Builder.Default
    private List<LineItemDto> lineItems = new ArrayList<>();

    @Builder.Default
    private Set<TagDto> tags = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
}
