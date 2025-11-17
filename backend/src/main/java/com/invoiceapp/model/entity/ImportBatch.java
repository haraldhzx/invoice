package com.invoiceapp.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "import_batches")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "import_type", nullable = false, length = 50)
    private String importType;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "total_records", nullable = false)
    private Integer totalRecords = 0;

    @Column(name = "successful_records", nullable = false)
    private Integer successfulRecords = 0;

    @Column(name = "failed_records", nullable = false)
    private Integer failedRecords = 0;

    @Column(nullable = false, length = 20)
    private String status = "PROCESSING";

    @Column(name = "error_log", columnDefinition = "TEXT")
    private String errorLog;

    @OneToMany(mappedBy = "importBatch", cascade = CascadeType.ALL)
    private List<Transaction> transactions = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public void incrementSuccessful() {
        this.successfulRecords++;
    }

    public void incrementFailed() {
        this.failedRecords++;
    }

    public void addError(String error) {
        if (this.errorLog == null) {
            this.errorLog = error;
        } else {
            this.errorLog += "\n" + error;
        }
    }
}
