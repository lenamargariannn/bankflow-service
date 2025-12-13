package com.bankflow.dto;

import com.bankflow.model.enums.AccountStatus;
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
public class AccountResponse {

    private Long id;

    private String accountNumber;

    private Long customerId;

    private BigDecimal balance;

    private AccountStatus status;

    private Long version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

