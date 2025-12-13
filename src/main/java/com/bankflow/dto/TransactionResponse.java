package com.bankflow.dto;

import com.bankflow.model.enums.TransactionType;
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
public class TransactionResponse {

    private Long id;

    private TransactionType type;

    private BigDecimal amount;

    private Long fromAccountId;

    private Long toAccountId;

    private LocalDateTime timestamp;

    private String description;

    private LocalDateTime createdAt;
}

