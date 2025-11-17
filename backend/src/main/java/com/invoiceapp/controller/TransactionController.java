package com.invoiceapp.controller;

import com.invoiceapp.model.dto.TransactionDto;
import com.invoiceapp.model.entity.ImportBatch;
import com.invoiceapp.model.entity.User;
import com.invoiceapp.repository.UserRepository;
import com.invoiceapp.service.CsvImportService;
import com.invoiceapp.service.TransactionService;
import com.opencsv.exceptions.CsvException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management and import endpoints")
public class TransactionController {

    private final TransactionService transactionService;
    private final CsvImportService csvImportService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all transactions", description = "Get all transactions for the authenticated user")
    public ResponseEntity<Page<TransactionDto>> getAllTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        User user = getUserFromDetails(userDetails);
        Page<TransactionDto> transactions = transactionService.getAllTransactions(user.getId(), pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Get a specific transaction by ID")
    public ResponseEntity<TransactionDto> getTransactionById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        TransactionDto transaction = transactionService.getTransactionById(id, user.getId());
        return ResponseEntity.ok(transaction);
    }

    @PostMapping
    @Operation(summary = "Create transaction", description = "Create a new transaction manually")
    public ResponseEntity<TransactionDto> createTransaction(
            @Valid @RequestBody TransactionDto transactionDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        TransactionDto created = transactionService.createTransaction(transactionDto, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transaction", description = "Update an existing transaction")
    public ResponseEntity<TransactionDto> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody TransactionDto transactionDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        TransactionDto updated = transactionService.updateTransaction(id, transactionDto, user.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction", description = "Delete a transaction")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        transactionService.deleteTransaction(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import/csv")
    @Operation(summary = "Import transactions from CSV", description = "Import bank transactions from CSV file")
    public ResponseEntity<Map<String, Object>> importTransactions(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException, CsvException {
        User user = getUserFromDetails(userDetails);
        ImportBatch batch = csvImportService.importBankTransactions(file, user.getId());

        Map<String, Object> response = Map.of(
                "batchId", batch.getId(),
                "status", batch.getStatus(),
                "totalRecords", batch.getTotalRecords(),
                "successfulRecords", batch.getSuccessfulRecords(),
                "failedRecords", batch.getFailedRecords(),
                "errorLog", batch.getErrorLog() != null ? batch.getErrorLog() : ""
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/unreconciled")
    @Operation(summary = "Get unreconciled transactions", description = "Get all unreconciled transactions")
    public ResponseEntity<List<TransactionDto>> getUnreconciledTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        List<TransactionDto> transactions = transactionService.getUnreconciledTransactions(user.getId());
        return ResponseEntity.ok(transactions);
    }

    private User getUserFromDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
