package com.invoiceapp.service;

import com.invoiceapp.model.dto.McpQueryResponse;
import com.invoiceapp.model.entity.Category;
import com.invoiceapp.model.enums.CategoryType;
import com.invoiceapp.repository.CategoryRepository;
import com.invoiceapp.repository.InvoiceRepository;
import com.invoiceapp.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class McpQueryService {

    private final InvoiceRepository invoiceRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final LlmService llmService;

    @Transactional(readOnly = true)
    public McpQueryResponse processQuery(String query, UUID userId) {
        log.info("Processing MCP query for user {}: {}", userId, query);

        String normalizedQuery = query.toLowerCase().trim();

        // Detect intent
        McpQueryResponse.QueryIntent intent = detectIntent(normalizedQuery);

        switch (intent) {
            case SPENDING_BY_CATEGORY:
                return handleSpendingByCategory(query, normalizedQuery, userId);

            case TOTAL_SPENDING:
                return handleTotalSpending(query, normalizedQuery, userId);

            case CATEGORY_BREAKDOWN:
                return handleCategoryBreakdown(query, normalizedQuery, userId);

            case RECENT_TRANSACTIONS:
                return handleRecentTransactions(query, normalizedQuery, userId);

            default:
                // Fallback to LLM for complex queries
                return handleWithLlm(query, userId);
        }
    }

    private McpQueryResponse.QueryIntent detectIntent(String query) {
        if (query.matches(".*(spend|spent|cost|pay|paid).*(on|for).*")) {
            return McpQueryResponse.QueryIntent.SPENDING_BY_CATEGORY;
        } else if (query.matches(".*(total|all|overall).*(spend|spent|cost).*")) {
            return McpQueryResponse.QueryIntent.TOTAL_SPENDING;
        } else if (query.matches(".*(breakdown|distribution|categories).*")) {
            return McpQueryResponse.QueryIntent.CATEGORY_BREAKDOWN;
        } else if (query.matches(".*(recent|last|latest).*(transaction|purchase|invoice).*")) {
            return McpQueryResponse.QueryIntent.RECENT_TRANSACTIONS;
        }
        return McpQueryResponse.QueryIntent.UNKNOWN;
    }

    private McpQueryResponse handleSpendingByCategory(String query, String normalizedQuery, UUID userId) {
        // Extract category from query
        String categoryName = extractCategory(normalizedQuery);
        LocalDate[] dateRange = extractDateRange(normalizedQuery);

        List<Category> categories = categoryRepository.findAvailableCategories(userId, CategoryType.EXPENSE);

        // Find matching category (fuzzy matching)
        Optional<Category> matchedCategory = categories.stream()
                .filter(c -> normalizedQuery.contains(c.getName().toLowerCase()) ||
                        c.getName().toLowerCase().contains(categoryName))
                .findFirst();

        if (matchedCategory.isEmpty()) {
            return McpQueryResponse.builder()
                    .query(query)
                    .answer("I couldn't find a category matching '" + categoryName + "'. Available categories: " +
                            categories.stream().map(Category::getName).limit(10).collect(Collectors.joining(", ")))
                    .intent(McpQueryResponse.QueryIntent.SPENDING_BY_CATEGORY)
                    .suggestions(List.of("Try: How much did I spend on Food & Dining?",
                            "Try: What did I spend on Transportation?"))
                    .build();
        }

        Category category = matchedCategory.get();
        BigDecimal totalInvoices = invoiceRepository.getTotalAmountByCategoryAndDateRange(
                userId, category.getId(), dateRange[0], dateRange[1]);
        BigDecimal totalTransactions = transactionRepository.getTotalAmountByCategoryAndDateRange(
                userId, category.getId(), dateRange[0], dateRange[1]);

        BigDecimal total = (totalInvoices != null ? totalInvoices : BigDecimal.ZERO)
                .add(totalTransactions != null ? totalTransactions : BigDecimal.ZERO);

        String period = formatDateRange(dateRange[0], dateRange[1]);
        String answer = String.format("You spent $%.2f on %s %s.",
                total, category.getName(), period);

        Map<String, Object> data = new HashMap<>();
        data.put("category", category.getName());
        data.put("amount", total);
        data.put("startDate", dateRange[0]);
        data.put("endDate", dateRange[1]);
        data.put("invoices", totalInvoices != null ? totalInvoices : BigDecimal.ZERO);
        data.put("transactions", totalTransactions != null ? totalTransactions : BigDecimal.ZERO);

        return McpQueryResponse.builder()
                .query(query)
                .answer(answer)
                .intent(McpQueryResponse.QueryIntent.SPENDING_BY_CATEGORY)
                .data(data)
                .suggestions(List.of(
                        "Show me a breakdown of all categories",
                        "What are my top expenses?",
                        "How does this compare to last month?"
                ))
                .build();
    }

    private McpQueryResponse handleTotalSpending(String query, String normalizedQuery, UUID userId) {
        LocalDate[] dateRange = extractDateRange(normalizedQuery);

        BigDecimal totalInvoices = invoiceRepository.getTotalAmountByUserAndDateRange(
                userId, dateRange[0], dateRange[1]);

        BigDecimal total = totalInvoices != null ? totalInvoices : BigDecimal.ZERO;

        String period = formatDateRange(dateRange[0], dateRange[1]);
        String answer = String.format("You spent a total of $%.2f %s.",
                total, period);

        Map<String, Object> data = new HashMap<>();
        data.put("totalAmount", total);
        data.put("startDate", dateRange[0]);
        data.put("endDate", dateRange[1]);

        return McpQueryResponse.builder()
                .query(query)
                .answer(answer)
                .intent(McpQueryResponse.QueryIntent.TOTAL_SPENDING)
                .data(data)
                .suggestions(List.of(
                        "Show me a breakdown by category",
                        "What were my biggest expenses?",
                        "How much did I spend last month?"
                ))
                .build();
    }

    private McpQueryResponse handleCategoryBreakdown(String query, String normalizedQuery, UUID userId) {
        LocalDate[] dateRange = extractDateRange(normalizedQuery);

        List<Category> categories = categoryRepository.findAvailableCategories(userId, CategoryType.EXPENSE);

        Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Category category : categories) {
            BigDecimal amount = invoiceRepository.getTotalAmountByCategoryAndDateRange(
                    userId, category.getId(), dateRange[0], dateRange[1]);

            if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                breakdown.put(category.getName(), amount);
                grandTotal = grandTotal.add(amount);
            }
        }

        // Sort by amount descending
        Map<String, BigDecimal> sortedBreakdown = breakdown.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        StringBuilder answer = new StringBuilder();
        String period = formatDateRange(dateRange[0], dateRange[1]);
        answer.append(String.format("Here's your spending breakdown %s:\n\n", period));

        BigDecimal finalGrandTotal = grandTotal;
        sortedBreakdown.forEach((cat, amt) -> {
            double percentage = amt.divide(finalGrandTotal, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
            answer.append(String.format("• %s: $%.2f (%.1f%%)\n", cat, amt, percentage));
        });

        answer.append(String.format("\nTotal: $%.2f", grandTotal));

        Map<String, Object> data = new HashMap<>();
        data.put("breakdown", sortedBreakdown);
        data.put("total", grandTotal);
        data.put("startDate", dateRange[0]);
        data.put("endDate", dateRange[1]);

        return McpQueryResponse.builder()
                .query(query)
                .answer(answer.toString())
                .intent(McpQueryResponse.QueryIntent.CATEGORY_BREAKDOWN)
                .data(data)
                .suggestions(List.of(
                        "How much did I spend on " + sortedBreakdown.keySet().iterator().next() + "?",
                        "Show me my spending trend over time",
                        "What can I cut back on?"
                ))
                .build();
    }

    private McpQueryResponse handleRecentTransactions(String query, String normalizedQuery, UUID userId) {
        // This is a placeholder - would need more implementation
        return McpQueryResponse.builder()
                .query(query)
                .answer("Recent transactions feature is under development.")
                .intent(McpQueryResponse.QueryIntent.RECENT_TRANSACTIONS)
                .suggestions(List.of("Try asking about spending by category"))
                .build();
    }

    private McpQueryResponse handleWithLlm(String query, UUID userId) {
        // Use LLM for complex queries
        String llmResponse = llmService.processQuery(query, userId.toString());

        return McpQueryResponse.builder()
                .query(query)
                .answer(llmResponse)
                .intent(McpQueryResponse.QueryIntent.UNKNOWN)
                .suggestions(List.of(
                        "How much did I spend on groceries?",
                        "Show me my spending breakdown",
                        "What's my total spending this month?"
                ))
                .build();
    }

    private String extractCategory(String query) {
        // Simple extraction - would be enhanced with NLP
        Pattern pattern = Pattern.compile("(?:on|for)\\s+([\\w\\s&]+?)(?:\\s+|\\?|$)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // Common category keywords
        if (query.contains("sweet") || query.contains("candy") || query.contains("dessert")) {
            return "sweets & desserts";
        }
        if (query.contains("house") || query.contains("housing") || query.contains("rent")) {
            return "housing";
        }
        if (query.contains("kids") || query.contains("children")) {
            return "kids";
        }
        if (query.contains("food") || query.contains("dining") || query.contains("restaurant")) {
            return "food & dining";
        }

        return "";
    }

    private LocalDate[] extractDateRange(String query) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        if (query.contains("this month")) {
            YearMonth currentMonth = YearMonth.now();
            startDate = currentMonth.atDay(1);
            endDate = currentMonth.atEndOfMonth();
        } else if (query.contains("last month")) {
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            startDate = lastMonth.atDay(1);
            endDate = lastMonth.atEndOfMonth();
        } else if (query.contains("this year")) {
            startDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);
            endDate = LocalDate.of(LocalDate.now().getYear(), 12, 31);
        } else if (query.contains("last year")) {
            int lastYear = LocalDate.now().getYear() - 1;
            startDate = LocalDate.of(lastYear, 1, 1);
            endDate = LocalDate.of(lastYear, 12, 31);
        } else {
            // Default: last 30 days
            startDate = endDate.minusDays(30);
        }

        return new LocalDate[]{startDate, endDate};
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        if (start.getMonth() == end.getMonth() && start.getYear() == end.getYear()) {
            return "in " + start.getMonth().toString() + " " + start.getYear();
        } else if (start.equals(end.minusDays(30))) {
            return "in the last 30 days";
        } else {
            return "from " + start.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) +
                    " to " + end.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        }
    }
}
