package com.bankflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update customer information. Only provided fields will be updated.")
public class UpdateCustomerRequest {

    @Schema(description = "Customer's full name", example = "John Smith")
    @Size(min = 1, message = "Full name cannot be empty")
    private String fullName;

    @Schema(description = "Email address", example = "john.smith@example.com")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(description = "Phone number (10-15 digits, optionally starting with +)", example = "+1234567890")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be 10-15 digits, optionally starting with +")
    private String phoneNumber;

}
