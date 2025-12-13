package com.bankflow.dto;

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
public class CreateAccountRequest {
    @DecimalMin(value = "0.00", message = "Initial deposit must be greater than or equal to 0")
    private BigDecimal initialDeposit;
}

