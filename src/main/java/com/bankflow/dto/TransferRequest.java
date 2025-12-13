package com.bankflow.dto;

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
public class TransferRequest {

    @NotNull(message = "Source account number cannot be null")
    @Pattern(regexp = "^[0-9]{12,20}$", message = "Account number must contain only digits and be between 12 and 20 characters long")
    private String fromAccountNumber;

    @NotNull(message = "Destination account number cannot be null")
    @Pattern(regexp = "^[0-9]{12,20}$", message = "Account number must contain only digits and be between 12 and 20 characters long")
    private String toAccountNumber;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than 0")
    private BigDecimal amount;

    private String description;
}

