package com.bankflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new account with optional initial deposit")
public class CreateAccountRequest {

    @Schema(description = "Initial deposit amount (must be >= 0)", example = "1000.00")
    @DecimalMin(value = "0.00", message = "Initial deposit must be greater than or equal to 0")
    private BigDecimal initialDeposit;
}

