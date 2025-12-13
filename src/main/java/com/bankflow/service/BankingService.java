package com.bankflow.service;

import com.bankflow.dto.UpdateCustomerRequest;
import com.bankflow.model.Account;
import com.bankflow.model.Customer;
import com.bankflow.model.TransactionRecord;

import java.math.BigDecimal;
import java.util.List;

public interface BankingService {
    Customer updateCustomer(String username, UpdateCustomerRequest request);

    Customer getCustomer(Long customerId);

    Customer getCustomerByUsername(String username);

    Account createAccount(String username, BigDecimal initialDeposit);

    void getAccount(Long accountId);

    Account getAccountByNumber(String accountNumber);

    List<Account> getCustomerAccountsByUsername(String username);

    TransactionRecord deposit(Long accountId, BigDecimal amount);

    TransactionRecord depositByAccountNumber(String accountNumber, BigDecimal amount);

    TransactionRecord withdraw(Long accountId, BigDecimal amount);

    TransactionRecord withdrawByAccountNumber(String accountNumber, BigDecimal amount);

    TransactionRecord transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String description);

    TransactionRecord transferByAccountNumber(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description);

    List<TransactionRecord> getTransactionsForAccount(Long accountId);

    List<TransactionRecord> getTransactionsByAccountNumber(String accountNumber);

    TransactionRecord getTransactionForAccount(Long accountId, Long transactionId);

    TransactionRecord getTransactionForAccountByNumber(String accountNumber, Long transactionId);
}
