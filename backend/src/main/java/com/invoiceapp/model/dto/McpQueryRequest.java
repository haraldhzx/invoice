package com.invoiceapp.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpQueryRequest {

    @NotBlank(message = "Query is required")
    private String query;

    private String context; // Optional context from previous queries
}
