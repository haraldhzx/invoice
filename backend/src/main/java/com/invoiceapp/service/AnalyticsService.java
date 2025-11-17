package com.invoiceapp.service;

import com.invoiceapp.model.dto.SpendingTrendDto;
import com.invoiceapp.model.dto.VendorAnalyticsDto;
import com.invoiceapp.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final InvoiceRepository invoiceRepository;

    /**
     * Get spending trends grouped by month
     */
    public List<SpendingTrendDto> getMonthlySpendingTrends(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = invoiceRepository.getMonthlySpending(userId, startDate, endDate);

        List<SpendingTrendDto> trends = new ArrayList<>();
        SpendingTrendDto previousTrend = null;

        for (Object[] row : results) {
            String period = (String) row[0]; // YYYY-MM
            BigDecimal amount = (BigDecimal) row[1];
            Long count = (Long) row[2];

            SpendingTrendDto trend = SpendingTrendDto.builder()
                    .period(period)
                    .date(LocalDate.parse(period + "-01"))
                    .amount(amount)
                    .transactionCount(count)
                    .averageAmount(count > 0 ? amount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                    .build();

            // Calculate percentage change from previous period
            if (previousTrend != null && previousTrend.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal change = amount.subtract(previousTrend.getAmount())
                        .divide(previousTrend.getAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                trend.setChange(change);
            }

            trends.add(trend);
            previousTrend = trend;
        }

        return trends;
    }

    /**
     * Get spending trends grouped by week
     */
    public List<SpendingTrendDto> getWeeklySpendingTrends(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = invoiceRepository.getWeeklySpending(userId, startDate, endDate);

        List<SpendingTrendDto> trends = new ArrayList<>();
        SpendingTrendDto previousTrend = null;

        for (Object[] row : results) {
            Integer year = (Integer) row[0];
            Integer week = (Integer) row[1];
            BigDecimal amount = (BigDecimal) row[2];
            Long count = (Long) row[3];

            String period = String.format("%d-W%02d", year, week);
            LocalDate weekStart = LocalDate.ofYearDay(year, 1).with(java.time.temporal.WeekFields.ISO.weekOfYear(), week);

            SpendingTrendDto trend = SpendingTrendDto.builder()
                    .period(period)
                    .date(weekStart)
                    .amount(amount)
                    .transactionCount(count)
                    .averageAmount(count > 0 ? amount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                    .build();

            if (previousTrend != null && previousTrend.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal change = amount.subtract(previousTrend.getAmount())
                        .divide(previousTrend.getAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                trend.setChange(change);
            }

            trends.add(trend);
            previousTrend = trend;
        }

        return trends;
    }

    /**
     * Get top vendors by spending
     */
    public List<VendorAnalyticsDto> getTopVendors(UUID userId, LocalDate startDate, LocalDate endDate, int limit) {
        List<Object[]> results = invoiceRepository.getVendorAnalytics(userId, startDate, endDate);

        return results.stream()
                .limit(limit)
                .map(row -> VendorAnalyticsDto.builder()
                        .vendorName((String) row[0])
                        .totalSpent((BigDecimal) row[1])
                        .invoiceCount((Long) row[2])
                        .averageAmount((BigDecimal) row[3])
                        .firstPurchase((LocalDate) row[4])
                        .lastPurchase((LocalDate) row[5])
                        .topCategory((String) row[6])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Detect recurring expenses
     */
    public List<Map<String, Object>> detectRecurringExpenses(UUID userId) {
        List<Object[]> results = invoiceRepository.findRecurringExpenses(userId);

        return results.stream()
                .map(row -> {
                    Map<String, Object> recurring = new HashMap<>();
                    recurring.put("vendorName", row[0]);
                    recurring.put("categoryName", row[1]);
                    recurring.put("averageAmount", row[2]);
                    recurring.put("frequency", row[3]); // Number of occurrences
                    recurring.put("averageDaysBetween", row[4]);
                    recurring.put("isRegular", row[5]); // Boolean if pattern is regular
                    return recurring;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get spending comparison between two periods
     */
    public Map<String, Object> compareSpendingPeriods(UUID userId,
                                                       LocalDate period1Start, LocalDate period1End,
                                                       LocalDate period2Start, LocalDate period2End) {
        BigDecimal period1Total = invoiceRepository.getTotalAmountByUserAndDateRange(userId, period1Start, period1End);
        BigDecimal period2Total = invoiceRepository.getTotalAmountByUserAndDateRange(userId, period2Start, period2End);

        Map<String, Object> comparison = new HashMap<>();
        comparison.put("period1Total", period1Total);
        comparison.put("period2Total", period2Total);
        comparison.put("difference", period2Total.subtract(period1Total));

        if (period1Total.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentageChange = period2Total.subtract(period1Total)
                    .divide(period1Total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            comparison.put("percentageChange", percentageChange);
        }

        // Category-wise comparison
        List<Object[]> period1Categories = invoiceRepository.getSpendingByCategory(userId, period1Start, period1End);
        List<Object[]> period2Categories = invoiceRepository.getSpendingByCategory(userId, period2Start, period2End);

        Map<String, BigDecimal> period1Map = period1Categories.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1]
                ));

        Map<String, BigDecimal> period2Map = period2Categories.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1]
                ));

        List<Map<String, Object>> categoryComparisons = new ArrayList<>();
        Set<String> allCategories = new HashSet<>();
        allCategories.addAll(period1Map.keySet());
        allCategories.addAll(period2Map.keySet());

        for (String category : allCategories) {
            BigDecimal period1Amount = period1Map.getOrDefault(category, BigDecimal.ZERO);
            BigDecimal period2Amount = period2Map.getOrDefault(category, BigDecimal.ZERO);

            Map<String, Object> categoryComp = new HashMap<>();
            categoryComp.put("category", category);
            categoryComp.put("period1Amount", period1Amount);
            categoryComp.put("period2Amount", period2Amount);
            categoryComp.put("difference", period2Amount.subtract(period1Amount));

            if (period1Amount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal change = period2Amount.subtract(period1Amount)
                        .divide(period1Amount, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                categoryComp.put("percentageChange", change);
            }

            categoryComparisons.add(categoryComp);
        }

        comparison.put("categoryComparisons", categoryComparisons);

        return comparison;
    }

    /**
     * Get spending forecast based on historical data
     */
    public Map<String, Object> getForecast(UUID userId, int monthsAhead) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(6); // Use last 6 months for forecast

        List<SpendingTrendDto> trends = getMonthlySpendingTrends(userId, startDate, endDate);

        if (trends.isEmpty()) {
            return Map.of("forecast", Collections.emptyList(), "confidence", "low");
        }

        // Simple moving average for forecast
        BigDecimal averageSpending = trends.stream()
                .map(SpendingTrendDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(trends.size()), 2, RoundingMode.HALF_UP);

        List<Map<String, Object>> forecast = new ArrayList<>();
        for (int i = 1; i <= monthsAhead; i++) {
            LocalDate forecastDate = endDate.plusMonths(i);
            Map<String, Object> monthForecast = new HashMap<>();
            monthForecast.put("month", forecastDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            monthForecast.put("predictedAmount", averageSpending);
            monthForecast.put("confidence", "medium");
            forecast.add(monthForecast);
        }

        return Map.of(
                "forecast", forecast,
                "averageMonthlySpending", averageSpending,
                "confidence", "medium",
                "basedOnMonths", trends.size()
        );
    }
}
