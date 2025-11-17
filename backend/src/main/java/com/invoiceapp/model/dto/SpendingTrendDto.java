package com.invoiceapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingTrendDto {
    private LocalDate date;
    private String period; // "2024-01" for monthly, "2024-W01" for weekly, "2024-01-01" for daily
    private BigDecimal amount;
    private Long transactionCount;
    private BigDecimal averageAmount;
    private BigDecimal change; // Percentage change from previous period
}
