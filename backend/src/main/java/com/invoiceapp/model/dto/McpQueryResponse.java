package com.invoiceapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpQueryResponse {
    private String query;
    private String answer;
    private QueryIntent intent;
    private Map<String, Object> data;
    private List<String> suggestions;

    public enum QueryIntent {
        SPENDING_BY_CATEGORY,
        TOTAL_SPENDING,
        SPENDING_BY_TIME,
        CATEGORY_BREAKDOWN,
        BUDGET_STATUS,
        RECENT_TRANSACTIONS,
        VENDOR_ANALYSIS,
        UNKNOWN
    }
}
