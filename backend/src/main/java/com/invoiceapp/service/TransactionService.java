package com.invoiceapp.service;

import com.invoiceapp.exception.ResourceNotFoundException;
import com.invoiceapp.exception.ValidationException;
import com.invoiceapp.model.dto.TransactionDto;
import com.invoiceapp.model.entity.Category;
import com.invoiceapp.model.entity.ImportBatch;
import com.invoiceapp.model.entity.Transaction;
import com.invoiceapp.model.entity.User;
import com.invoiceapp.model.enums.TransactionType;
import com.invoiceapp.repository.CategoryRepository;
import com.invoiceapp.repository.ImportBatchRepository;
import com.invoiceapp.repository.TransactionRepository;
import com.invoiceapp.repository.UserRepository;
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
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ImportBatchRepository importBatchRepository;

    @Transactional(readOnly = true)
    public Page<TransactionDto> getAllTransactions(UUID userId, Pageable pageable) {
        log.debug("Fetching transactions for user: {}", userId);
        Page<Transaction> transactions = transactionRepository.findByUserId(userId, pageable);
        return transactions.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(UUID id, UUID userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new ValidationException("You don't have access to this transaction");
        }

        return mapToDto(transaction);
    }

    @Transactional
    public TransactionDto createTransaction(TransactionDto dto, UUID userId) {
        log.info("Creating transaction for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Transaction transaction = Transaction.builder()
                .user(user)
                .transactionDate(dto.getTransactionDate())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .type(dto.getType())
                .balance(dto.getBalance())
                .bankName(dto.getBankName())
                .accountNumber(dto.getAccountNumber())
                .referenceNumber(dto.getReferenceNumber())
                .notes(dto.getNotes())
                .isReconciled(false)
                .build();

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            transaction.setCategory(category);
        }

        transaction = transactionRepository.save(transaction);
        log.info("Transaction created with ID: {}", transaction.getId());

        return mapToDto(transaction);
    }

    @Transactional
    public TransactionDto updateTransaction(UUID id, TransactionDto dto, UUID userId) {
        log.info("Updating transaction: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new ValidationException("You don't have access to this transaction");
        }

        transaction.setTransactionDate(dto.getTransactionDate());
        transaction.setDescription(dto.getDescription());
        transaction.setAmount(dto.getAmount());
        transaction.setCurrency(dto.getCurrency());
        transaction.setType(dto.getType());
        transaction.setBalance(dto.getBalance());
        transaction.setNotes(dto.getNotes());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            transaction.setCategory(category);
        }

        transaction = transactionRepository.save(transaction);
        log.info("Transaction updated: {}", id);

        return mapToDto(transaction);
    }

    @Transactional
    public void deleteTransaction(UUID id, UUID userId) {
        log.info("Deleting transaction: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new ValidationException("You don't have access to this transaction");
        }

        transactionRepository.delete(transaction);
        log.info("Transaction deleted: {}", id);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalByCategory(UUID userId, UUID categoryId, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = transactionRepository.getTotalAmountByCategoryAndDateRange(
                userId, categoryId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getUnreconciledTransactions(UUID userId) {
        List<Transaction> transactions = transactionRepository.findUnreconciledTransactions(userId);
        return transactions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private TransactionDto mapToDto(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .transactionDate(transaction.getTransactionDate())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .type(transaction.getType())
                .balance(transaction.getBalance())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .invoiceId(transaction.getInvoice() != null ? transaction.getInvoice().getId() : null)
                .bankName(transaction.getBankName())
                .accountNumber(transaction.getAccountNumber())
                .referenceNumber(transaction.getReferenceNumber())
                .isReconciled(transaction.getIsReconciled())
                .reconciledAt(transaction.getReconciledAt())
                .notes(transaction.getNotes())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
