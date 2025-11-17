package com.invoiceapp.service;

import com.invoiceapp.exception.ResourceNotFoundException;
import com.invoiceapp.exception.ValidationException;
import com.invoiceapp.model.dto.InvoiceAnalysisResult;
import com.invoiceapp.model.dto.InvoiceDto;
import com.invoiceapp.model.entity.*;
import com.invoiceapp.model.enums.InvoiceStatus;
import com.invoiceapp.repository.CategoryRepository;
import com.invoiceapp.repository.InvoiceRepository;
import com.invoiceapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceProcessingService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StorageService storageService;
    private final OcrService ocrService;
    private final LlmService llmService;

    @Transactional
    public InvoiceDto uploadAndProcessInvoice(MultipartFile file, UUID userId) throws IOException {
        log.info("Processing uploaded invoice for user: {}", userId);

        // Validate file
        validateFile(file);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Create invoice with PROCESSING status
        Invoice invoice = Invoice.builder()
                .user(user)
                .status(InvoiceStatus.PROCESSING)
                .currency("USD")
                .build();

        invoice = invoiceRepository.save(invoice);

        try {
            // Store file
            String storageKey = storageService.store(file, "invoices");
            String storageUrl = storageService.getUrl(storageKey);

            // Create attachment
            Attachment attachment = Attachment.builder()
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .storageKey(storageKey)
                    .storageUrl(storageUrl)
                    .build();
            invoice.addAttachment(attachment);

            // Extract text using OCR
            byte[] fileBytes = file.getBytes();
            String extractedText = ocrService.extractText(fileBytes, file.getContentType());
            log.debug("OCR extracted {} characters", extractedText.length());

            // Analyze with LLM
            InvoiceAnalysisResult analysis = llmService.analyzeInvoice(fileBytes, extractedText);
            log.info("LLM analysis completed with confidence: {}", analysis.getConfidence());

            // Update invoice with extracted data
            updateInvoiceFromAnalysis(invoice, analysis, user);

            // Save extracted data as JSON
            Map<String, Object> extractedData = new HashMap<>();
            extractedData.put("ocrText", extractedText);
            extractedData.put("llmProvider", llmService.getProviderName());
            extractedData.put("analysisTimestamp", LocalDateTime.now().toString());
            invoice.setExtractedData(extractedData);

            // Mark as completed or review required based on confidence
            if (analysis.getConfidence() != null && analysis.getConfidence().compareTo(new BigDecimal("0.7")) >= 0) {
                invoice.setStatus(InvoiceStatus.COMPLETED);
            } else {
                invoice.setStatus(InvoiceStatus.REVIEW_REQUIRED);
            }

            invoice.setProcessedAt(LocalDateTime.now());
            invoice = invoiceRepository.save(invoice);

            log.info("Invoice processed successfully with ID: {}", invoice.getId());
            return mapToDto(invoice);

        } catch (Exception e) {
            log.error("Error processing invoice", e);
            invoice.setStatus(InvoiceStatus.FAILED);
            invoiceRepository.save(invoice);
            throw new RuntimeException("Failed to process invoice: " + e.getMessage(), e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ValidationException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new ValidationException("Only image and PDF files are supported");
        }

        // Max file size: 10MB
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new ValidationException("File size exceeds 10MB limit");
        }
    }

    private void updateInvoiceFromAnalysis(Invoice invoice, InvoiceAnalysisResult analysis, User user) {
        invoice.setVendorName(analysis.getVendorName());
        invoice.setInvoiceNumber(analysis.getInvoiceNumber());
        invoice.setDate(analysis.getDate() != null ? analysis.getDate() : invoice.getCreatedAt().toLocalDate());
        invoice.setDueDate(analysis.getDueDate());
        invoice.setTotalAmount(analysis.getTotalAmount() != null ? analysis.getTotalAmount() : BigDecimal.ZERO);
        invoice.setCurrency(analysis.getCurrency() != null ? analysis.getCurrency() : "USD");
        invoice.setTaxAmount(analysis.getTaxAmount());
        invoice.setConfidence(analysis.getConfidence());
        invoice.setPaymentMethod(analysis.getPaymentMethod());

        // Find matching category
        if (analysis.getSuggestedCategory() != null) {
            List<Category> categories = categoryRepository.findAvailableCategories(
                    user.getId(),
                    com.invoiceapp.model.enums.CategoryType.EXPENSE
            );

            categories.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(analysis.getSuggestedCategory()))
                    .findFirst()
                    .ifPresent(invoice::setCategory);
        }

        // Add line items
        if (analysis.getLineItems() != null && !analysis.getLineItems().isEmpty()) {
            analysis.getLineItems().forEach(extractedItem -> {
                LineItem lineItem = LineItem.builder()
                        .description(extractedItem.getDescription())
                        .quantity(extractedItem.getQuantity() != null ? extractedItem.getQuantity() : BigDecimal.ONE)
                        .unitPrice(extractedItem.getUnitPrice() != null ? extractedItem.getUnitPrice() : BigDecimal.ZERO)
                        .totalPrice(extractedItem.getTotalPrice() != null ? extractedItem.getTotalPrice() : BigDecimal.ZERO)
                        .category(extractedItem.getCategory())
                        .build();
                invoice.addLineItem(lineItem);
            });
        }
    }

    private InvoiceDto mapToDto(Invoice invoice) {
        return InvoiceDto.builder()
                .id(invoice.getId())
                .vendorName(invoice.getVendorName())
                .invoiceNumber(invoice.getInvoiceNumber())
                .date(invoice.getDate())
                .dueDate(invoice.getDueDate())
                .totalAmount(invoice.getTotalAmount())
                .currency(invoice.getCurrency())
                .taxAmount(invoice.getTaxAmount())
                .categoryId(invoice.getCategory() != null ? invoice.getCategory().getId() : null)
                .categoryName(invoice.getCategory() != null ? invoice.getCategory().getName() : null)
                .subcategoryId(invoice.getSubcategory() != null ? invoice.getSubcategory().getId() : null)
                .subcategoryName(invoice.getSubcategory() != null ? invoice.getSubcategory().getName() : null)
                .status(invoice.getStatus())
                .confidence(invoice.getConfidence())
                .extractedData(invoice.getExtractedData())
                .notes(invoice.getNotes())
                .paymentMethod(invoice.getPaymentMethod())
                .isRecurring(invoice.getIsRecurring())
                .recurringFrequency(invoice.getRecurringFrequency())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .processedAt(invoice.getProcessedAt())
                .build();
    }
}
