package com.invoiceapp.controller;

import com.invoiceapp.model.dto.SpendingTrendDto;
import com.invoiceapp.model.dto.VendorAnalyticsDto;
import com.invoiceapp.model.entity.User;
import com.invoiceapp.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Enhanced analytics and insights endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/trends/monthly")
    @Operation(summary = "Get monthly spending trends", description = "Returns spending trends grouped by month with percentage changes")
    public ResponseEntity<List<SpendingTrendDto>> getMonthlyTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getUserFromDetails(userDetails);
        List<SpendingTrendDto> trends = analyticsService.getMonthlySpendingTrends(user.getId(), startDate, endDate);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/trends/weekly")
    @Operation(summary = "Get weekly spending trends", description = "Returns spending trends grouped by week")
    public ResponseEntity<List<SpendingTrendDto>> getWeeklyTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getUserFromDetails(userDetails);
        List<SpendingTrendDto> trends = analyticsService.getWeeklySpendingTrends(user.getId(), startDate, endDate);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/vendors/top")
    @Operation(summary = "Get top vendors by spending", description = "Returns top vendors ordered by total spending")
    public ResponseEntity<List<VendorAnalyticsDto>> getTopVendors(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getUserFromDetails(userDetails);
        List<VendorAnalyticsDto> vendors = analyticsService.getTopVendors(user.getId(), startDate, endDate, limit);
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/recurring-expenses")
    @Operation(summary = "Detect recurring expenses", description = "Identifies recurring payment patterns")
    public ResponseEntity<List<Map<String, Object>>> getRecurringExpenses(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getUserFromDetails(userDetails);
        List<Map<String, Object>> recurring = analyticsService.detectRecurringExpenses(user.getId());
        return ResponseEntity.ok(recurring);
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare two time periods", description = "Compares spending between two date ranges")
    public ResponseEntity<Map<String, Object>> comparePeriods(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period1Start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period1End,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period2Start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period2End,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getUserFromDetails(userDetails);
        Map<String, Object> comparison = analyticsService.compareSpendingPeriods(
                user.getId(), period1Start, period1End, period2Start, period2End);
        return ResponseEntity.ok(comparison);
    }

    @GetMapping("/forecast")
    @Operation(summary = "Get spending forecast", description = "Predicts future spending based on historical data")
    public ResponseEntity<Map<String, Object>> getForecast(
            @RequestParam(defaultValue = "3") int monthsAhead,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getUserFromDetails(userDetails);
        Map<String, Object> forecast = analyticsService.getForecast(user.getId(), monthsAhead);
        return ResponseEntity.ok(forecast);
    }

    private User getUserFromDetails(UserDetails userDetails) {
        // This would normally fetch from UserRepository
        // For now, simplified implementation
        User user = new User();
        user.setEmail(userDetails.getUsername());
        // Set ID from security context or fetch from repository
        return user;
    }
}
