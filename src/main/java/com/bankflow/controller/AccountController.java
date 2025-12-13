package com.bankflow.controller;

import com.bankflow.dto.AccountResponse;
import com.bankflow.dto.AmountRequest;
import com.bankflow.dto.TransactionResponse;
import com.bankflow.dto.TransferRequest;
import com.bankflow.model.Account;
import com.bankflow.model.TransactionRecord;
import com.bankflow.service.BankingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final BankingService bankingService;

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        log.info("GET /accounts/{} - Retrieving account details", accountNumber);
        Account account = bankingService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account.mapToResponse());
    }

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody AmountRequest request) {
        log.info("POST /accounts/{}/deposit - Depositing amount: {}", accountNumber, request.getAmount());
        TransactionRecord transaction = bankingService.depositByAccountNumber(accountNumber, request.getAmount());
        return ResponseEntity.ok(transaction.mapToTransactionResponse());
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable String accountNumber,
            @Valid @RequestBody AmountRequest request) {
        log.info("POST /accounts/{}/withdraw - Withdrawing amount: {}", accountNumber, request.getAmount());
        TransactionRecord transaction = bankingService.withdrawByAccountNumber(accountNumber, request.getAmount());
        return ResponseEntity.ok(transaction.mapToTransactionResponse());
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("POST /accounts/transfer - Transferring {} from account {} to account {}",
                request.getAmount(), request.getFromAccountNumber(), request.getToAccountNumber());
        TransactionRecord transaction = bankingService.transferByAccountNumber(
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount(),
                request.getDescription()
        );
        return ResponseEntity.ok(transaction.mapToTransactionResponse());
    }

    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable String accountNumber) {
        log.info("GET /accounts/{}/transactions - Retrieving transactions", accountNumber);
        List<TransactionRecord> transactions = bankingService.getTransactionsByAccountNumber(accountNumber);
        List<TransactionResponse> responses = transactions.stream()
                .map(TransactionRecord::mapToTransactionResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{accountNumber}/transactions/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionForAccount(
            @PathVariable String accountNumber,
            @PathVariable Long transactionId) {
        log.info("GET /accounts/{}/transactions/{} - Retrieving specific transaction", accountNumber, transactionId);
        TransactionRecord transaction = bankingService.getTransactionForAccountByNumber(accountNumber, transactionId);
        return ResponseEntity.ok(transaction.mapToTransactionResponse());
    }
}
