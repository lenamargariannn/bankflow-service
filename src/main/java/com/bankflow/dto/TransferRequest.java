package com.bankflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for transferring money between accounts")
public class TransferRequest {

    @Schema(description = "Source account number (12-20 digits)", example = "123456789012")
    @NotNull(message = "Source account number cannot be null")
    @Pattern(regexp = "^[0-9]{12,20}$", message = "Account number must contain only digits and be between 12 and 20 characters long")
    private String fromAccountNumber;

    @Schema(description = "Destination account number (12-20 digits)", example = "987654321098")
    @NotNull(message = "Destination account number cannot be null")
    @Pattern(regexp = "^[0-9]{12,20}$", message = "Account number must contain only digits and be between 12 and 20 characters long")
    private String toAccountNumber;

    @Schema(description = "Amount to transfer", example = "250.75")
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than 0")
    private BigDecimal amount;

    @Schema(description = "Optional description for the transfer", example = "Payment for invoice #1234")
    private String description;
}

