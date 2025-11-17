package com.invoiceapp.repository;

import com.invoiceapp.model.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByUserId(UUID userId);

    List<Budget> findByUserIdAndIsActive(UUID userId, Boolean isActive);

    List<Budget> findByUserIdAndCategoryId(UUID userId, UUID categoryId);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.isActive = true AND b.startDate <= :date AND (b.endDate IS NULL OR b.endDate >= :date)")
    List<Budget> findActiveBudgetsForDate(@Param("userId") UUID userId, @Param("date") LocalDate date);
}
