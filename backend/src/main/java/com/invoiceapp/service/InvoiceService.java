package com.invoiceapp.service;

import com.invoiceapp.exception.ResourceNotFoundException;
import com.invoiceapp.exception.ValidationException;
import com.invoiceapp.model.dto.CreateInvoiceRequest;
import com.invoiceapp.model.dto.InvoiceDto;
import com.invoiceapp.model.entity.*;
import com.invoiceapp.model.enums.InvoiceStatus;
import com.invoiceapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public Page<InvoiceDto> getAllInvoices(UUID userId, Pageable pageable) {
        log.debug("Fetching invoices for user: {} with pagination", userId);
        Page<Invoice> invoices = invoiceRepository.findByUserId(userId, pageable);
        return invoices.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceDto> getInvoicesByStatus(UUID userId, InvoiceStatus status, Pageable pageable) {
        log.debug("Fetching invoices by status {} for user: {}", status, userId);
        Page<Invoice> invoices = invoiceRepository.findByUserIdAndStatus(userId, status, pageable);
        return invoices.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceDto> getInvoicesByCategory(UUID userId, UUID categoryId, Pageable pageable) {
        log.debug("Fetching invoices by category {} for user: {}", categoryId, userId);
        Page<Invoice> invoices = invoiceRepository.findByUserIdAndCategoryId(userId, categoryId, pageable);
        return invoices.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceById(UUID id, UUID userId) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        // Ensure user owns this invoice
        if (!invoice.getUser().getId().equals(userId)) {
            throw new ValidationException("You don't have access to this invoice");
        }

        return mapToDto(invoice);
    }

    @Transactional
    public InvoiceDto createInvoice(CreateInvoiceRequest request, UUID userId) {
        log.info("Creating invoice for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Invoice invoice = Invoice.builder()
                .user(user)
                .vendorName(request.getVendorName())
                .invoiceNumber(request.getInvoiceNumber())
                .date(request.getDate())
                .dueDate(request.getDueDate())
                .totalAmount(request.getTotalAmount())
                .currency(request.getCurrency())
                .taxAmount(request.getTaxAmount())
                .status(InvoiceStatus.COMPLETED)
                .notes(request.getNotes())
                .paymentMethod(request.getPaymentMethod())
                .isRecurring(request.getIsRecurring() != null && request.getIsRecurring())
                .recurringFrequency(request.getRecurringFrequency())
                .build();

        // Set category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            invoice.setCategory(category);
        }

        // Set subcategory if provided
        if (request.getSubcategoryId() != null) {
            Category subcategory = categoryRepository.findById(request.getSubcategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getSubcategoryId()));
            invoice.setSubcategory(subcategory);
        }

        // Add line items if provided
        if (request.getLineItems() != null && !request.getLineItems().isEmpty()) {
            request.getLineItems().forEach(lineItemDto -> {
                LineItem lineItem = LineItem.builder()
                        .description(lineItemDto.getDescription())
                        .quantity(lineItemDto.getQuantity())
                        .unitPrice(lineItemDto.getUnitPrice())
                        .totalPrice(lineItemDto.getQuantity().multiply(lineItemDto.getUnitPrice()))
                        .category(lineItemDto.getCategory())
                        .sku(lineItemDto.getSku())
                        .build();
                invoice.addLineItem(lineItem);
            });
        }

        // Add tags if provided
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            request.getTags().forEach(tagName -> {
                Tag tag = tagRepository.findByUserIdAndName(userId, tagName)
                        .orElseGet(() -> {
                            Tag newTag = Tag.builder()
                                    .user(user)
                                    .name(tagName)
                                    .build();
                            return tagRepository.save(newTag);
                        });
                invoice.addTag(tag);
            });
        }

        invoice = invoiceRepository.save(invoice);
        log.info("Invoice created with ID: {}", invoice.getId());

        return mapToDto(invoice);
    }

    @Transactional
    public InvoiceDto updateInvoice(UUID id, CreateInvoiceRequest request, UUID userId) {
        log.info("Updating invoice: {}", id);

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        // Ensure user owns this invoice
        if (!invoice.getUser().getId().equals(userId)) {
            throw new ValidationException("You don't have access to this invoice");
        }

        // Update fields
        invoice.setVendorName(request.getVendorName());
        invoice.setInvoiceNumber(request.getInvoiceNumber());
        invoice.setDate(request.getDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setTotalAmount(request.getTotalAmount());
        invoice.setCurrency(request.getCurrency());
        invoice.setTaxAmount(request.getTaxAmount());
        invoice.setNotes(request.getNotes());
        invoice.setPaymentMethod(request.getPaymentMethod());
        invoice.setIsRecurring(request.getIsRecurring() != null && request.getIsRecurring());
        invoice.setRecurringFrequency(request.getRecurringFrequency());

        // Update category
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            invoice.setCategory(category);
        } else {
            invoice.setCategory(null);
        }

        // Update subcategory
        if (request.getSubcategoryId() != null) {
            Category subcategory = categoryRepository.findById(request.getSubcategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getSubcategoryId()));
            invoice.setSubcategory(subcategory);
        } else {
            invoice.setSubcategory(null);
        }

        invoice = invoiceRepository.save(invoice);
        log.info("Invoice updated: {}", id);

        return mapToDto(invoice);
    }

    @Transactional
    public void deleteInvoice(UUID id, UUID userId) {
        log.info("Deleting invoice: {}", id);

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        // Ensure user owns this invoice
        if (!invoice.getUser().getId().equals(userId)) {
            throw new ValidationException("You don't have access to this invoice");
        }

        invoiceRepository.delete(invoice);
        log.info("Invoice deleted: {}", id);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalSpending(UUID userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating total spending for user: {} between {} and {}", userId, startDate, endDate);
        BigDecimal total = invoiceRepository.getTotalAmountByUserAndDateRange(userId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getSpendingByCategory(UUID userId, UUID categoryId, LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating spending by category {} for user: {} between {} and {}",
                categoryId, userId, startDate, endDate);
        BigDecimal total = invoiceRepository.getTotalAmountByCategoryAndDateRange(userId, categoryId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
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
