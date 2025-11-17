package com.invoiceapp.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayerUpdateRequest {

    @Size(max = 100, message = "Name must be less than 100 characters")
    private String name;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;

    @Size(max = 20, message = "Phone number must be less than 20 characters")
    private String phoneNumber;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @Size(max = 50, message = "Color must be less than 50 characters")
    private String color;

    @Size(max = 50, message = "Icon must be less than 50 characters")
    private String icon;

    private Boolean isDefault;

    private Boolean active;
}
