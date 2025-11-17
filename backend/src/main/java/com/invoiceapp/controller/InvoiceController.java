package com.invoiceapp.controller;

import com.invoiceapp.model.dto.CreateInvoiceRequest;
import com.invoiceapp.model.dto.InvoiceDto;
import com.invoiceapp.model.entity.User;
import com.invoiceapp.model.enums.InvoiceStatus;
import com.invoiceapp.repository.UserRepository;
import com.invoiceapp.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice management endpoints")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all invoices", description = "Get all invoices for the authenticated user with pagination")
    public ResponseEntity<Page<InvoiceDto>> getAllInvoices(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        User user = getUserFromDetails(userDetails);
        Page<InvoiceDto> invoices = invoiceService.getAllInvoices(user.getId(), pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get invoices by status", description = "Get invoices filtered by status")
    public ResponseEntity<Page<InvoiceDto>> getInvoicesByStatus(
            @PathVariable InvoiceStatus status,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        User user = getUserFromDetails(userDetails);
        Page<InvoiceDto> invoices = invoiceService.getInvoicesByStatus(user.getId(), status, pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get invoices by category", description = "Get invoices filtered by category")
    public ResponseEntity<Page<InvoiceDto>> getInvoicesByCategory(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        User user = getUserFromDetails(userDetails);
        Page<InvoiceDto> invoices = invoiceService.getInvoicesByCategory(user.getId(), categoryId, pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID", description = "Get a specific invoice by ID")
    public ResponseEntity<InvoiceDto> getInvoiceById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        InvoiceDto invoice = invoiceService.getInvoiceById(id, user.getId());
        return ResponseEntity.ok(invoice);
    }

    @PostMapping
    @Operation(summary = "Create invoice", description = "Create a new invoice manually")
    public ResponseEntity<InvoiceDto> createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        InvoiceDto created = invoiceService.createInvoice(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update invoice", description = "Update an existing invoice")
    public ResponseEntity<InvoiceDto> updateInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody CreateInvoiceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        InvoiceDto updated = invoiceService.updateInvoice(id, request, user.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete invoice", description = "Delete an invoice")
    public ResponseEntity<Void> deleteInvoice(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        invoiceService.deleteInvoice(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/analytics/total-spending")
    @Operation(summary = "Get total spending", description = "Get total spending for a date range")
    public ResponseEntity<BigDecimal> getTotalSpending(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Start date") LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "End date") LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        BigDecimal total = invoiceService.getTotalSpending(user.getId(), startDate, endDate);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/analytics/spending-by-category/{categoryId}")
    @Operation(summary = "Get spending by category", description = "Get spending for a specific category in a date range")
    public ResponseEntity<BigDecimal> getSpendingByCategory(
            @PathVariable UUID categoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Start date") LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "End date") LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        BigDecimal total = invoiceService.getSpendingByCategory(user.getId(), categoryId, startDate, endDate);
        return ResponseEntity.ok(total);
    }

    private User getUserFromDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
