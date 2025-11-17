package com.invoiceapp.service;

import com.invoiceapp.exception.ResourceNotFoundException;
import com.invoiceapp.model.entity.ImportBatch;
import com.invoiceapp.model.entity.Transaction;
import com.invoiceapp.model.entity.User;
import com.invoiceapp.model.enums.TransactionType;
import com.invoiceapp.repository.ImportBatchRepository;
import com.invoiceapp.repository.TransactionRepository;
import com.invoiceapp.repository.UserRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvImportService {

    private final UserRepository userRepository;
    private final ImportBatchRepository importBatchRepository;
    private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    };

    @Transactional
    public ImportBatch importBankTransactions(MultipartFile file, UUID userId) throws IOException, CsvException {
        log.info("Starting CSV import for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Create import batch
        ImportBatch batch = ImportBatch.builder()
                .user(user)
                .importType("BANK_TRANSACTION")
                .fileName(file.getOriginalFilename())
                .status("PROCESSING")
                .totalRecords(0)
                .successfulRecords(0)
                .failedRecords(0)
                .build();

        batch = importBatchRepository.save(batch);

        List<Transaction> transactions = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();

            if (rows.isEmpty()) {
                batch.setStatus("FAILED");
                batch.setErrorLog("CSV file is empty");
                batch.setCompletedAt(LocalDateTime.now());
                importBatchRepository.save(batch);
                return batch;
            }

            // Assume first row is header
            String[] headers = rows.get(0);
            int dateIndex = findColumnIndex(headers, "date", "transaction date", "trans date");
            int descIndex = findColumnIndex(headers, "description", "desc", "memo", "details");
            int amountIndex = findColumnIndex(headers, "amount", "value", "debit", "credit");
            int balanceIndex = findColumnIndex(headers, "balance", "running balance");

            batch.setTotalRecords(rows.size() - 1);

            // Process data rows
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);

                try {
                    Transaction transaction = parseTransaction(row, dateIndex, descIndex, amountIndex, balanceIndex, user, batch);
                    transactions.add(transaction);
                    batch.incrementSuccessful();
                } catch (Exception e) {
                    log.error("Error parsing row {}: {}", i, e.getMessage());
                    batch.incrementFailed();
                    batch.addError(String.format("Row %d: %s", i, e.getMessage()));
                }
            }

            // Save all transactions
            transactionRepository.saveAll(transactions);

            // Update batch status
            if (batch.getFailedRecords() == 0) {
                batch.setStatus("COMPLETED");
            } else if (batch.getSuccessfulRecords() > 0) {
                batch.setStatus("PARTIAL");
            } else {
                batch.setStatus("FAILED");
            }

            batch.setCompletedAt(LocalDateTime.now());
            batch = importBatchRepository.save(batch);

            log.info("CSV import completed: {} successful, {} failed",
                    batch.getSuccessfulRecords(), batch.getFailedRecords());

            return batch;

        } catch (Exception e) {
            log.error("Error importing CSV", e);
            batch.setStatus("FAILED");
            batch.setErrorLog(e.getMessage());
            batch.setCompletedAt(LocalDateTime.now());
            importBatchRepository.save(batch);
            throw e;
        }
    }

    private int findColumnIndex(String[] headers, String... possibleNames) {
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].toLowerCase().trim();
            for (String name : possibleNames) {
                if (header.contains(name.toLowerCase())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private Transaction parseTransaction(String[] row, int dateIndex, int descIndex,
                                         int amountIndex, int balanceIndex,
                                         User user, ImportBatch batch) {
        LocalDate date = parseDate(row[dateIndex]);
        String description = descIndex >= 0 && descIndex < row.length ? row[descIndex] : "";
        BigDecimal amount = parseAmount(row[amountIndex]);
        BigDecimal balance = balanceIndex >= 0 && balanceIndex < row.length ? parseAmount(row[balanceIndex]) : null;

        TransactionType type = amount.compareTo(BigDecimal.ZERO) < 0 ? TransactionType.DEBIT : TransactionType.CREDIT;

        return Transaction.builder()
                .user(user)
                .importBatch(batch)
                .transactionDate(date)
                .description(description)
                .amount(amount.abs())
                .currency("USD")
                .type(type)
                .balance(balance)
                .isReconciled(false)
                .build();
    }

    private LocalDate parseDate(String dateStr) {
        String cleaned = dateStr.trim();

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(cleaned, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }

        throw new IllegalArgumentException("Unable to parse date: " + dateStr);
    }

    private BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Remove currency symbols, commas, and spaces
        String cleaned = amountStr.trim()
                .replace("$", "")
                .replace("€", "")
                .replace("£", "")
                .replace(",", "")
                .replace(" ", "");

        // Handle parentheses for negative numbers
        if (cleaned.startsWith("(") && cleaned.endsWith(")")) {
            cleaned = "-" + cleaned.substring(1, cleaned.length() - 1);
        }

        return new BigDecimal(cleaned);
    }
}
