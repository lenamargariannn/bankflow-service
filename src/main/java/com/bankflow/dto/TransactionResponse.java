package com.bankflow.dto;

import com.bankflow.model.enums.TransactionType;
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
@Schema(description = "Transaction details")
public class TransactionResponse {

    @Schema(description = "Transaction ID", example = "1")
    private Long id;

    @Schema(description = "Transaction type", example = "TRANSFER")
    private TransactionType type;

    @Schema(description = "Transaction amount", example = "250.75")
    private BigDecimal amount;

    @Schema(description = "Source account ID (for transfers and withdrawals)", example = "1")
    private Long fromAccountId;

    @Schema(description = "Destination account ID (for transfers and deposits)", example = "2")
    private Long toAccountId;

    @Schema(description = "Transaction timestamp", example = "2025-12-15T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Transaction description", example = "Payment for invoice #1234")
    private String description;

    @Schema(description = "Record creation timestamp", example = "2025-12-15T10:30:00")
    private LocalDateTime createdAt;
}

