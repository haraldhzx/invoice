package com.invoiceapp.repository;

import com.invoiceapp.model.entity.Invoice;
import com.invoiceapp.model.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Page<Invoice> findByUserId(UUID userId, Pageable pageable);

    Page<Invoice> findByUserIdAndStatus(UUID userId, InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByUserIdAndCategoryId(UUID userId, UUID categoryId, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.user.id = :userId AND i.date BETWEEN :startDate AND :endDate")
    List<Invoice> findByUserIdAndDateRange(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT i FROM Invoice i WHERE i.user.id = :userId AND LOWER(i.vendorName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Invoice> searchByVendorName(@Param("userId") UUID userId, @Param("search") String search, Pageable pageable);

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.user.id = :userId AND i.date BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByUserAndDateRange(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.user.id = :userId AND i.category.id = :categoryId AND i.date BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByCategoryAndDateRange(
        @Param("userId") UUID userId,
        @Param("categoryId") UUID categoryId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT i FROM Invoice i JOIN i.tags t WHERE i.user.id = :userId AND t.id = :tagId")
    Page<Invoice> findByUserIdAndTagId(@Param("userId") UUID userId, @Param("tagId") UUID tagId, Pageable pageable);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.user.id = :userId AND i.status = :status")
    long countByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") InvoiceStatus status);

    @Query("SELECT i FROM Invoice i WHERE i.status = :status AND i.createdAt < :threshold")
    List<Invoice> findStaleInvoices(@Param("status") InvoiceStatus status, @Param("threshold") java.time.LocalDateTime threshold);
}
