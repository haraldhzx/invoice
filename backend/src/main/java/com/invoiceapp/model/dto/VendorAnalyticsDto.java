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
public class VendorAnalyticsDto {
    private String vendorName;
    private BigDecimal totalSpent;
    private Long invoiceCount;
    private BigDecimal averageAmount;
    private LocalDate firstPurchase;
    private LocalDate lastPurchase;
    private String topCategory;
}
