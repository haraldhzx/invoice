package com.invoiceapp.model.dto;

import com.invoiceapp.model.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private UUID id;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    private BigDecimal balance;

    private UUID categoryId;
    private String categoryName;

    private UUID invoiceId;

    private String bankName;
    private String accountNumber;
    private String referenceNumber;

    private Boolean isReconciled;
    private LocalDateTime reconciledAt;

    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
