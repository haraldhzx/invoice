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
public class AttachmentDto {
    private UUID id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String storageUrl;
    private String thumbnailUrl;
    private Integer width;
    private Integer height;
    private Integer pageCount;
    private LocalDateTime createdAt;
}
