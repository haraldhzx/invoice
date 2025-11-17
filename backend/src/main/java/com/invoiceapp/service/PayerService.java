package com.invoiceapp.service;

import com.invoiceapp.model.dto.PayerCreateRequest;
import com.invoiceapp.model.dto.PayerDto;
import com.invoiceapp.model.dto.PayerUpdateRequest;
import com.invoiceapp.model.entity.Payer;
import com.invoiceapp.model.entity.User;
import com.invoiceapp.repository.PayerRepository;
import com.invoiceapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PayerService {

    private final PayerRepository payerRepository;
    private final UserRepository userRepository;

    /**
     * Get all active payers for a user
     */
    @Transactional(readOnly = true)
    public List<PayerDto> getAllPayers(UUID userId) {
        return payerRepository.findByUserIdAndActiveTrue(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific payer by ID
     */
    @Transactional(readOnly = true)
    public PayerDto getPayerById(UUID userId, UUID payerId) {
        Payer payer = payerRepository.findByIdAndUserId(payerId, userId)
                .orElseThrow(() -> new RuntimeException("Payer not found"));
        return mapToDto(payer);
    }

    /**
     * Create a new payer
     */
    public PayerDto createPayer(UUID userId, PayerCreateRequest request) {
        // Check if payer name already exists
        if (payerRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new RuntimeException("Payer with name '" + request.getName() + "' already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If this is the first payer or marked as default, set as default
        boolean shouldBeDefault = request.getIsDefault() != null && request.getIsDefault();
        if (shouldBeDefault) {
            // Clear any existing default payer
            payerRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        payerRepository.save(existingDefault);
                    });
        } else if (payerRepository.countByUserIdAndActiveTrue(userId) == 0) {
            // Make this the default if it's the first payer
            shouldBeDefault = true;
        }

        Payer payer = Payer.builder()
                .user(user)
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .description(request.getDescription())
                .color(request.getColor())
                .icon(request.getIcon())
                .isDefault(shouldBeDefault)
                .active(true)
                .build();

        payer = payerRepository.save(payer);
        log.info("Created payer: {} for user: {}", payer.getName(), userId);
        return mapToDto(payer);
    }

    /**
     * Update an existing payer
     */
    public PayerDto updatePayer(UUID userId, UUID payerId, PayerUpdateRequest request) {
        Payer payer = payerRepository.findByIdAndUserId(payerId, userId)
                .orElseThrow(() -> new RuntimeException("Payer not found"));

        // Check if name is being changed and if it conflicts
        if (request.getName() != null && !request.getName().equals(payer.getName())) {
            if (payerRepository.existsByUserIdAndNameAndIdNot(userId, request.getName(), payerId)) {
                throw new RuntimeException("Payer with name '" + request.getName() + "' already exists");
            }
            payer.setName(request.getName());
        }

        if (request.getEmail() != null) {
            payer.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            payer.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDescription() != null) {
            payer.setDescription(request.getDescription());
        }
        if (request.getColor() != null) {
            payer.setColor(request.getColor());
        }
        if (request.getIcon() != null) {
            payer.setIcon(request.getIcon());
        }
        if (request.getActive() != null) {
            payer.setActive(request.getActive());
        }

        // Handle default payer changes
        if (request.getIsDefault() != null && request.getIsDefault() && !payer.getIsDefault()) {
            // Clear any existing default payer
            payerRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        payerRepository.save(existingDefault);
                    });
            payer.setIsDefault(true);
        }

        payer = payerRepository.save(payer);
        log.info("Updated payer: {} for user: {}", payer.getName(), userId);
        return mapToDto(payer);
    }

    /**
     * Delete a payer (soft delete by setting active = false)
     */
    public void deletePayer(UUID userId, UUID payerId) {
        Payer payer = payerRepository.findByIdAndUserId(payerId, userId)
                .orElseThrow(() -> new RuntimeException("Payer not found"));

        payer.setActive(false);
        if (payer.getIsDefault()) {
            payer.setIsDefault(false);
        }
        payerRepository.save(payer);
        log.info("Deleted (deactivated) payer: {} for user: {}", payer.getName(), userId);
    }

    /**
     * Get the default payer for a user
     */
    @Transactional(readOnly = true)
    public PayerDto getDefaultPayer(UUID userId) {
        Payer payer = payerRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElse(null);
        return payer != null ? mapToDto(payer) : null;
    }

    /**
     * Map Payer entity to DTO
     */
    private PayerDto mapToDto(Payer payer) {
        return PayerDto.builder()
                .id(payer.getId())
                .userId(payer.getUser().getId())
                .name(payer.getName())
                .email(payer.getEmail())
                .phoneNumber(payer.getPhoneNumber())
                .description(payer.getDescription())
                .color(payer.getColor())
                .icon(payer.getIcon())
                .isDefault(payer.getIsDefault())
                .active(payer.getActive())
                .createdAt(payer.getCreatedAt())
                .updatedAt(payer.getUpdatedAt())
                .build();
    }
}
