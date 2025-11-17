package com.invoiceapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayerDto {
    private UUID id;
    private UUID userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String description;
    private String color;
    private String icon;
    private Boolean isDefault;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
