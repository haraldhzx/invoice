package com.invoiceapp.controller;

import com.invoiceapp.model.dto.PayerCreateRequest;
import com.invoiceapp.model.dto.PayerDto;
import com.invoiceapp.model.dto.PayerUpdateRequest;
import com.invoiceapp.service.PayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payers")
@RequiredArgsConstructor
public class PayerController {

    private final PayerService payerService;

    /**
     * Get all active payers for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<PayerDto>> getAllPayers(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<PayerDto> payers = payerService.getAllPayers(userId);
        return ResponseEntity.ok(payers);
    }

    /**
     * Get a specific payer by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PayerDto> getPayerById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        PayerDto payer = payerService.getPayerById(userId, id);
        return ResponseEntity.ok(payer);
    }

    /**
     * Create a new payer
     */
    @PostMapping
    public ResponseEntity<PayerDto> createPayer(
            @Valid @RequestBody PayerCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        PayerDto payer = payerService.createPayer(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payer);
    }

    /**
     * Update an existing payer
     */
    @PutMapping("/{id}")
    public ResponseEntity<PayerDto> updatePayer(
            @PathVariable UUID id,
            @Valid @RequestBody PayerUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        PayerDto payer = payerService.updatePayer(userId, id, request);
        return ResponseEntity.ok(payer);
    }

    /**
     * Delete a payer (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayer(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        payerService.deletePayer(userId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get the default payer for the authenticated user
     */
    @GetMapping("/default")
    public ResponseEntity<PayerDto> getDefaultPayer(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        PayerDto payer = payerService.getDefaultPayer(userId);
        if (payer == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(payer);
    }
}
