package com.invoiceapp.controller;

import com.invoiceapp.model.dto.BudgetDto;
import com.invoiceapp.model.entity.User;
import com.invoiceapp.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Budget management and tracking endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    @Operation(summary = "Get all budgets", description = "Returns all budgets for the authenticated user")
    public ResponseEntity<List<BudgetDto>> getAllBudgets(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        List<BudgetDto> budgets = budgetService.getAllBudgets(user.getId());
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID", description = "Returns a specific budget")
    public ResponseEntity<BudgetDto> getBudgetById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        BudgetDto budget = budgetService.getBudgetById(id);
        return ResponseEntity.ok(budget);
    }

    @PostMapping
    @Operation(summary = "Create budget", description = "Creates a new budget")
    public ResponseEntity<BudgetDto> createBudget(
            @Valid @RequestBody BudgetDto budgetDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        BudgetDto created = budgetService.createBudget(budgetDto, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update budget", description = "Updates an existing budget")
    public ResponseEntity<BudgetDto> updateBudget(
            @PathVariable UUID id,
            @Valid @RequestBody BudgetDto budgetDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        BudgetDto updated = budgetService.updateBudget(id, budgetDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete budget", description = "Deletes a budget")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exceeded")
    @Operation(summary = "Get exceeded budgets", description = "Returns budgets that have been exceeded")
    public ResponseEntity<List<BudgetDto>> getExceededBudgets(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        List<BudgetDto> exceeded = budgetService.getExceededBudgets(user.getId());
        return ResponseEntity.ok(exceeded);
    }

    @GetMapping("/nearing-limit")
    @Operation(summary = "Get budgets nearing limit", description = "Returns budgets approaching their limit")
    public ResponseEntity<List<BudgetDto>> getBudgetsNearingLimit(
            @RequestParam(defaultValue = "80") int threshold,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        List<BudgetDto> nearing = budgetService.getBudgetsNearingLimit(user.getId(), threshold);
        return ResponseEntity.ok(nearing);
    }

    private User getUserFromDetails(UserDetails userDetails) {
        User user = new User();
        user.setEmail(userDetails.getUsername());
        return user;
    }
}
