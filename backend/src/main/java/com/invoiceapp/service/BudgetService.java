package com.invoiceapp.service;

import com.invoiceapp.model.dto.BudgetDto;
import com.invoiceapp.model.entity.Budget;
import com.invoiceapp.model.entity.BudgetPeriod;
import com.invoiceapp.model.entity.Category;
import com.invoiceapp.model.entity.User;
import com.invoiceapp.repository.BudgetRepository;
import com.invoiceapp.repository.CategoryRepository;
import com.invoiceapp.repository.InvoiceRepository;
import com.invoiceapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public List<BudgetDto> getAllBudgets(UUID userId) {
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        return budgets.stream()
                .map(this::mapToDtoWithSpending)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BudgetDto getBudgetById(UUID id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        return mapToDtoWithSpending(budget);
    }

    @Transactional
    public BudgetDto createBudget(BudgetDto dto, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
        }

        // Calculate start and end dates based on period
        LocalDate[] dates = calculatePeriodDates(dto.getPeriod(), dto.getStartDate());

        Budget budget = Budget.builder()
                .name(dto.getName())
                .user(user)
                .category(category)
                .amount(dto.getAmount())
                .period(dto.getPeriod())
                .startDate(dates[0])
                .endDate(dates[1])
                .alertEnabled(dto.getAlertEnabled() != null ? dto.getAlertEnabled() : true)
                .alertThreshold(dto.getAlertThreshold() != null ? dto.getAlertThreshold() : 80)
                .build();

        budget = budgetRepository.save(budget);
        return mapToDtoWithSpending(budget);
    }

    @Transactional
    public BudgetDto updateBudget(UUID id, BudgetDto dto) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        if (dto.getName() != null) {
            budget.setName(dto.getName());
        }
        if (dto.getAmount() != null) {
            budget.setAmount(dto.getAmount());
        }
        if (dto.getPeriod() != null) {
            budget.setPeriod(dto.getPeriod());
            LocalDate[] dates = calculatePeriodDates(dto.getPeriod(), budget.getStartDate());
            budget.setStartDate(dates[0]);
            budget.setEndDate(dates[1]);
        }
        if (dto.getAlertEnabled() != null) {
            budget.setAlertEnabled(dto.getAlertEnabled());
        }
        if (dto.getAlertThreshold() != null) {
            budget.setAlertThreshold(dto.getAlertThreshold());
        }
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            budget.setCategory(category);
        }

        budget = budgetRepository.save(budget);
        return mapToDtoWithSpending(budget);
    }

    @Transactional
    public void deleteBudget(UUID id) {
        if (!budgetRepository.existsById(id)) {
            throw new RuntimeException("Budget not found");
        }
        budgetRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<BudgetDto> getExceededBudgets(UUID userId) {
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        return budgets.stream()
                .map(this::mapToDtoWithSpending)
                .filter(BudgetDto::getIsExceeded)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BudgetDto> getBudgetsNearingLimit(UUID userId, int thresholdPercentage) {
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        return budgets.stream()
                .map(this::mapToDtoWithSpending)
                .filter(dto -> dto.getPercentageUsed() >= thresholdPercentage && !dto.getIsExceeded())
                .collect(Collectors.toList());
    }

    private BudgetDto mapToDtoWithSpending(Budget budget) {
        // Calculate spending for the budget period
        BigDecimal spent = BigDecimal.ZERO;

        if (budget.getCategory() != null) {
            spent = invoiceRepository.getTotalAmountByCategoryAndDateRange(
                    budget.getUser().getId(),
                    budget.getCategory().getId(),
                    budget.getStartDate(),
                    budget.getEndDate()
            );
        } else {
            spent = invoiceRepository.getTotalAmountByUserAndDateRange(
                    budget.getUser().getId(),
                    budget.getStartDate(),
                    budget.getEndDate()
            );
        }

        if (spent == null) {
            spent = BigDecimal.ZERO;
        }

        BigDecimal remaining = budget.getAmount().subtract(spent);
        double percentageUsed = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                ? spent.divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        return BudgetDto.builder()
                .id(budget.getId())
                .name(budget.getName())
                .categoryId(budget.getCategory() != null ? budget.getCategory().getId() : null)
                .categoryName(budget.getCategory() != null ? budget.getCategory().getName() : "All Categories")
                .amount(budget.getAmount())
                .period(budget.getPeriod())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .spent(spent)
                .remaining(remaining)
                .percentageUsed(percentageUsed)
                .isExceeded(spent.compareTo(budget.getAmount()) > 0)
                .alertEnabled(budget.isAlertEnabled())
                .alertThreshold(budget.getAlertThreshold())
                .build();
    }

    private LocalDate[] calculatePeriodDates(BudgetPeriod period, LocalDate referenceDate) {
        LocalDate start;
        LocalDate end;
        LocalDate ref = referenceDate != null ? referenceDate : LocalDate.now();

        switch (period) {
            case WEEKLY:
                start = ref.with(java.time.DayOfWeek.MONDAY);
                end = start.plusDays(6);
                break;
            case MONTHLY:
                YearMonth month = YearMonth.from(ref);
                start = month.atDay(1);
                end = month.atEndOfMonth();
                break;
            case QUARTERLY:
                int quarter = (ref.getMonthValue() - 1) / 3;
                start = LocalDate.of(ref.getYear(), quarter * 3 + 1, 1);
                end = start.plusMonths(3).minusDays(1);
                break;
            case YEARLY:
                start = LocalDate.of(ref.getYear(), 1, 1);
                end = LocalDate.of(ref.getYear(), 12, 31);
                break;
            case CUSTOM:
            default:
                start = ref;
                end = ref.plusMonths(1);
                break;
        }

        return new LocalDate[]{start, end};
    }
}
