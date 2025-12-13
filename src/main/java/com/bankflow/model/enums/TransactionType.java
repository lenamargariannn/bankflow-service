package com.bankflow.model.enums;

import lombok.Getter;

@Getter
public enum TransactionType {
    DEPOSIT("Deposit"),
    WITHDRAW("Withdraw"),
    TRANSFER("Transfer");
    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }
}

