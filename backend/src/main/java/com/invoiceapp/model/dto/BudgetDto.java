package com.invoiceapp.model.dto;

import com.invoiceapp.model.entity.BudgetPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDto {
    private UUID id;
    private String name;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal amount;
    private BudgetPeriod period;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal spent;
    private BigDecimal remaining;
    private Double percentageUsed;
    private Boolean isExceeded;
    private Boolean alertEnabled;
    private Integer alertThreshold; // Alert when X% of budget is used
}
