package com.invoiceapp.repository;

import com.invoiceapp.model.entity.Payer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayerRepository extends JpaRepository<Payer, UUID> {

    /**
     * Find all active payers for a specific user
     */
    List<Payer> findByUserIdAndActiveTrue(UUID userId);

    /**
     * Find all payers for a specific user (including inactive)
     */
    List<Payer> findByUserId(UUID userId);

    /**
     * Find a payer by ID and user ID
     */
    Optional<Payer> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Find the default payer for a user
     */
    Optional<Payer> findByUserIdAndIsDefaultTrue(UUID userId);

    /**
     * Check if a payer name exists for a user
     */
    boolean existsByUserIdAndNameAndIdNot(UUID userId, String name, UUID id);

    /**
     * Check if a payer name exists for a user (for create)
     */
    boolean existsByUserIdAndName(UUID userId, String name);

    /**
     * Count active payers for a user
     */
    long countByUserIdAndActiveTrue(UUID userId);

    /**
     * Get payers with invoice counts
     */
    @Query("SELECT p, COUNT(i) as invoiceCount FROM Payer p " +
           "LEFT JOIN Invoice i ON i.payer.id = p.id " +
           "WHERE p.user.id = :userId " +
           "GROUP BY p.id " +
           "ORDER BY invoiceCount DESC")
    List<Object[]> findPayersWithInvoiceCount(@Param("userId") UUID userId);
}
