package com.bankflow.exception;

import com.bankflow.model.enums.AccountStatus;
import lombok.Getter;

@Getter
public class AccountInactiveException extends RuntimeException {

    private final Long accountId;
    private final AccountStatus currentStatus;

    public AccountInactiveException(Long accountId, AccountStatus currentStatus) {
        super(String.format("Account %d is %s and cannot perform this operation", accountId, currentStatus.getDisplayName()));
        this.accountId = accountId;
        this.currentStatus = currentStatus;
    }

}
