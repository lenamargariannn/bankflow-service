package com.bankflow.exception;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class InsufficientFundsException extends RuntimeException {

    private final BigDecimal requiredAmount;
    private final BigDecimal availableAmount;
    private final Long accountId;

    public InsufficientFundsException(Long accountId, BigDecimal requiredAmount, BigDecimal availableAmount) {
        super(String.format("Account %d has insufficient funds. Required: %s, Available: %s",
                accountId, requiredAmount, availableAmount));
        this.accountId = accountId;
        this.requiredAmount = requiredAmount;
        this.availableAmount = availableAmount;
    }

}
