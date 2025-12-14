package com.bankflow.dto;

import com.bankflow.model.enums.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Account information")
public class AccountResponse {

    @Schema(description = "Account ID", example = "1")
    private Long id;

    @Schema(description = "Account number (12-20 digits)", example = "123456789012")
    private String accountNumber;

    @Schema(description = "Customer ID", example = "1")
    private Long customerId;

    @Schema(description = "Current balance", example = "1500.75")
    private BigDecimal balance;

    @Schema(description = "Account status", example = "ACTIVE")
    private AccountStatus status;

    @Schema(description = "Version number for optimistic locking", example = "0")
    private Long version;

    @Schema(description = "Account creation timestamp", example = "2025-12-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-12-15T10:30:00")
    private LocalDateTime updatedAt;
}

