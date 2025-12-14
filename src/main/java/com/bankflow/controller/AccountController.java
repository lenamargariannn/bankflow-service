package com.bankflow.controller;

import com.bankflow.dto.AccountResponse;
import com.bankflow.dto.AmountRequest;
import com.bankflow.dto.TransactionResponse;
import com.bankflow.dto.TransferRequest;
import com.bankflow.model.Account;
import com.bankflow.model.TransactionRecord;
import com.bankflow.service.BankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Accounts", description = "Account operations including transactions, deposits, withdrawals, and transfers")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final BankingService bankingService;

    @Operation(
            summary = "Get account details",
            description = "Retrieve account information by account number"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber) {
        log.info("GET /accounts/{} - Retrieving account details", accountNumber);
        Account account = bankingService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account.mapToResponse());
    }

    @Operation(
            summary = "Deposit money",
            description = "Deposit a specified amount into an account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deposit successful",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "400", description = "Invalid amount"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber,
            @Valid @RequestBody AmountRequest request) {
        log.info("POST /accounts/{}/deposit - Depositing amount: {}", accountNumber, request.getAmount());
        TransactionRecord transaction = bankingService.depositByAccountNumber(accountNumber, request.getAmount());
        return ResponseEntity.ok(transaction.mapToTransactionResponse());
    }

    @Operation(
            summary = "Withdraw money",
            description = "Withdraw a specified amount from an account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Withdrawal successful",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient funds"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber,
            @Valid @RequestBody AmountRequest request) {
        log.info("POST /accounts/{}/withdraw - Withdrawing amount: {}", accountNumber, request.getAmount());
        TransactionRecord transaction = bankingService.withdrawByAccountNumber(accountNumber, request.getAmount());
        return ResponseEntity.ok(transaction.mapToTransactionResponse());
    }

    @Operation(
            summary = "Transfer money",
            description = "Transfer money from one account to another"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer successful",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "400", description = "Invalid amount, insufficient funds, or same account transfer"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
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

    @Operation(
            summary = "Get account transactions",
            description = "Retrieve all transactions for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber) {
        log.info("GET /accounts/{}/transactions - Retrieving transactions", accountNumber);
        List<TransactionRecord> transactions = bankingService.getTransactionsByAccountNumber(accountNumber);
        List<TransactionResponse> responses = transactions.stream()
                .map(TransactionRecord::mapToTransactionResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(
            summary = "Get specific transaction",
            description = "Retrieve a specific transaction for an account by transaction ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transaction or account not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{accountNumber}/transactions/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionForAccount(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber,
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable Long transactionId) {
        log.info("GET /accounts/{}/transactions/{} - Retrieving specific transaction", accountNumber, transactionId);
        TransactionRecord transaction = bankingService.getTransactionForAccountByNumber(accountNumber, transactionId);
        return ResponseEntity.ok(transaction.mapToTransactionResponse());
    }
}
