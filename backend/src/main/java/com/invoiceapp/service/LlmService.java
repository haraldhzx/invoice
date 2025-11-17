package com.invoiceapp.service;

import com.invoiceapp.model.dto.InvoiceAnalysisResult;

public interface LlmService {

    /**
     * Analyze an invoice image and extract structured data
     *
     * @param imageBytes Image bytes
     * @param extractedText OCR text (optional, can enhance accuracy)
     * @return Analysis result with extracted invoice data
     */
    InvoiceAnalysisResult analyzeInvoice(byte[] imageBytes, String extractedText);

    /**
     * Process a natural language query about spending
     *
     * @param query Natural language query
     * @param userId User ID for context
     * @return Response text
     */
    String processQuery(String query, String userId);

    /**
     * Get the configured LLM provider name
     */
    String getProviderName();
}
