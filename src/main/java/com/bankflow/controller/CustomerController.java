package com.bankflow.controller;

import com.bankflow.dto.AccountResponse;
import com.bankflow.dto.CreateAccountRequest;
import com.bankflow.dto.CustomerResponse;
import com.bankflow.dto.UpdateCustomerRequest;
import com.bankflow.model.Account;
import com.bankflow.model.Customer;
import com.bankflow.service.BankingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final BankingService bankingService;

    @PutMapping("/{username}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable String username, @Valid @RequestBody UpdateCustomerRequest request) {
        log.info("PUT /customers/{}", username);
        Customer updatedCustomer = bankingService.updateCustomer(username, request);
        return ResponseEntity.ok(updatedCustomer.mapToResponse());
    }

    @GetMapping("/{username}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable String username) {
        log.info("GET /customers/{} - Retrieving customer details by username", username);
        Customer customer = bankingService.getCustomerByUsername(username);
        return ResponseEntity.ok(customer.mapToResponse());
    }

    @GetMapping("/{username}/accounts")
    public ResponseEntity<List<AccountResponse>> listCustomerAccounts(@PathVariable String username) {
        log.info("GET /customers/{}/accounts - Listing all accounts for customer", username);
        List<Account> accounts = bankingService.getCustomerAccountsByUsername(username);
        List<AccountResponse> responses = accounts.stream()
                .map(Account::mapToResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{username}/accounts")
    public ResponseEntity<AccountResponse> createAccount(
            @PathVariable String username,
            @Valid @RequestBody CreateAccountRequest request) {
        log.info("POST /customers/{}/accounts - Creating new account", username);
        Account createdAccount = bankingService.createAccount(username, request.getInitialDeposit());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount.mapToResponse());
    }
}
