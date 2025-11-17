package com.invoiceapp.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payer entity representing individuals who pay for invoices
 * Useful for tracking expenses by different people (family members, team members, etc.)
 */
@Entity
@Table(name = "payers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 500)
    private String description;

    @Column(length = 50)
    private String color; // For UI display (e.g., "#FF5722")

    @Column(length = 50)
    private String icon; // Icon identifier for UI

    @Column(nullable = false)
    private Boolean isDefault = false; // Mark the current user as default payer

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
