package com.invoiceapp.repository;

import com.invoiceapp.model.entity.ImportBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImportBatchRepository extends JpaRepository<ImportBatch, UUID> {

    Page<ImportBatch> findByUserId(UUID userId, Pageable pageable);

    Page<ImportBatch> findByUserIdAndStatus(UUID userId, String status, Pageable pageable);
}
