package com.invoiceapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceAnalysisResult {
    private String vendorName;
    private String invoiceNumber;
    private LocalDate date;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private String currency;
    private BigDecimal taxAmount;
    private String suggestedCategory;
    private String suggestedSubcategory;
    private BigDecimal confidence;
    private String paymentMethod;

    @Builder.Default
    private List<ExtractedLineItem> lineItems = new ArrayList<>();

    private String vendorAddress;
    private String vendorPhone;
    private String vendorEmail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedLineItem {
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String category;
    }
}
