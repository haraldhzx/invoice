package com.invoiceapp.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoiceapp.config.LlmProperties;
import com.invoiceapp.model.dto.InvoiceAnalysisResult;
import com.invoiceapp.service.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "openai", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class OpenAiLlmService implements LlmService {

    private final LlmProperties llmProperties;
    private final ObjectMapper objectMapper;
    private WebClient webClient;

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = WebClient.builder()
                    .baseUrl("https://api.openai.com/v1")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + llmProperties.getOpenai().getApiKey())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
        }
        return webClient;
    }

    @Override
    public InvoiceAnalysisResult analyzeInvoice(byte[] imageBytes, String extractedText) {
        log.info("Analyzing invoice with OpenAI GPT-4 Vision");

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        String prompt = buildInvoiceAnalysisPrompt(extractedText);

        Map<String, Object> requestBody = Map.of(
                "model", llmProperties.getOpenai().getModel(),
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of("type", "text", "text", prompt),
                                        Map.of("type", "image_url",
                                                "image_url", Map.of("url", "data:image/jpeg;base64," + base64Image))
                                )
                        )
                ),
                "max_tokens", llmProperties.getOpenai().getMaxTokens()
        );

        try {
            String response = getWebClient()
                    .post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseOpenAiResponse(response);
        } catch (Exception e) {
            log.error("Error analyzing invoice with OpenAI", e);
            throw new RuntimeException("Failed to analyze invoice: " + e.getMessage(), e);
        }
    }

    @Override
    public String processQuery(String query, String userId) {
        log.info("Processing query with OpenAI: {}", query);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4",
                "messages", List.of(
                        Map.of("role", "system", "content",
                                "You are a helpful financial assistant that answers questions about spending and expenses."),
                        Map.of("role", "user", "content", query)
                ),
                "max_tokens", 500
        );

        try {
            String response = getWebClient()
                    .post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("Error processing query with OpenAI", e);
            throw new RuntimeException("Failed to process query: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "OpenAI";
    }

    private String buildInvoiceAnalysisPrompt(String extractedText) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an invoice analysis expert. Analyze the following invoice image and extract structured data.\n\n");

        if (extractedText != null && !extractedText.isEmpty()) {
            prompt.append("OCR Text extracted from the image:\n");
            prompt.append(extractedText);
            prompt.append("\n\n");
        }

        prompt.append("Extract the following information and return it as a JSON object:\n");
        prompt.append("{\n");
        prompt.append("  \"vendorName\": \"<vendor/merchant name>\",\n");
        prompt.append("  \"invoiceNumber\": \"<invoice/receipt number>\",\n");
        prompt.append("  \"date\": \"<date in YYYY-MM-DD format>\",\n");
        prompt.append("  \"dueDate\": \"<due date in YYYY-MM-DD format, if available>\",\n");
        prompt.append("  \"totalAmount\": <total amount as number>,\n");
        prompt.append("  \"currency\": \"<currency code, e.g., USD>\",\n");
        prompt.append("  \"taxAmount\": <tax amount as number, if available>,\n");
        prompt.append("  \"suggestedCategory\": \"<suggested expense category>\",\n");
        prompt.append("  \"confidence\": <confidence score between 0 and 1>,\n");
        prompt.append("  \"lineItems\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"description\": \"<item description>\",\n");
        prompt.append("      \"quantity\": <quantity>,\n");
        prompt.append("      \"unitPrice\": <unit price>,\n");
        prompt.append("      \"totalPrice\": <total price>\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"vendorAddress\": \"<vendor address, if available>\",\n");
        prompt.append("  \"vendorPhone\": \"<vendor phone, if available>\"\n");
        prompt.append("}\n\n");
        prompt.append("Categories: Food & Dining, Transportation, Shopping, Entertainment, Housing, Utilities, Healthcare, Education, etc.\n");
        prompt.append("Return ONLY the JSON object, no additional text.");

        return prompt.toString();
    }

    private InvoiceAnalysisResult parseOpenAiResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            String content = jsonNode.path("choices").get(0).path("message").path("content").asText();

            // Extract JSON from the response (may have markdown code blocks)
            String jsonString = content;
            if (content.contains("```json")) {
                jsonString = content.substring(content.indexOf("```json") + 7);
                jsonString = jsonString.substring(0, jsonString.indexOf("```"));
            } else if (content.contains("```")) {
                jsonString = content.substring(content.indexOf("```") + 3);
                jsonString = jsonString.substring(0, jsonString.indexOf("```"));
            }

            JsonNode dataNode = objectMapper.readTree(jsonString.trim());

            InvoiceAnalysisResult.InvoiceAnalysisResultBuilder builder = InvoiceAnalysisResult.builder();

            if (dataNode.has("vendorName")) {
                builder.vendorName(dataNode.get("vendorName").asText());
            }
            if (dataNode.has("invoiceNumber")) {
                builder.invoiceNumber(dataNode.get("invoiceNumber").asText());
            }
            if (dataNode.has("date") && !dataNode.get("date").isNull()) {
                builder.date(LocalDate.parse(dataNode.get("date").asText(), DateTimeFormatter.ISO_DATE));
            }
            if (dataNode.has("dueDate") && !dataNode.get("dueDate").isNull()) {
                builder.dueDate(LocalDate.parse(dataNode.get("dueDate").asText(), DateTimeFormatter.ISO_DATE));
            }
            if (dataNode.has("totalAmount")) {
                builder.totalAmount(new BigDecimal(dataNode.get("totalAmount").asText()));
            }
            if (dataNode.has("currency")) {
                builder.currency(dataNode.get("currency").asText());
            }
            if (dataNode.has("taxAmount") && !dataNode.get("taxAmount").isNull()) {
                builder.taxAmount(new BigDecimal(dataNode.get("taxAmount").asText()));
            }
            if (dataNode.has("suggestedCategory")) {
                builder.suggestedCategory(dataNode.get("suggestedCategory").asText());
            }
            if (dataNode.has("confidence")) {
                builder.confidence(new BigDecimal(dataNode.get("confidence").asText()));
            }
            if (dataNode.has("vendorAddress")) {
                builder.vendorAddress(dataNode.get("vendorAddress").asText());
            }
            if (dataNode.has("vendorPhone")) {
                builder.vendorPhone(dataNode.get("vendorPhone").asText());
            }

            // Parse line items
            if (dataNode.has("lineItems") && dataNode.get("lineItems").isArray()) {
                List<InvoiceAnalysisResult.ExtractedLineItem> lineItems = new ArrayList<>();
                for (JsonNode itemNode : dataNode.get("lineItems")) {
                    InvoiceAnalysisResult.ExtractedLineItem lineItem = InvoiceAnalysisResult.ExtractedLineItem.builder()
                            .description(itemNode.has("description") ? itemNode.get("description").asText() : null)
                            .quantity(itemNode.has("quantity") ? new BigDecimal(itemNode.get("quantity").asText()) : null)
                            .unitPrice(itemNode.has("unitPrice") ? new BigDecimal(itemNode.get("unitPrice").asText()) : null)
                            .totalPrice(itemNode.has("totalPrice") ? new BigDecimal(itemNode.get("totalPrice").asText()) : null)
                            .build();
                    lineItems.add(lineItem);
                }
                builder.lineItems(lineItems);
            }

            return builder.build();
        } catch (Exception e) {
            log.error("Error parsing OpenAI response", e);
            throw new RuntimeException("Failed to parse OpenAI response: " + e.getMessage(), e);
        }
    }
}
