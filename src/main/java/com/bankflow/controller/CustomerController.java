package com.bankflow.controller;

import com.bankflow.dto.AccountResponse;
import com.bankflow.dto.CreateAccountRequest;
import com.bankflow.dto.CustomerResponse;
import com.bankflow.dto.UpdateCustomerRequest;
import com.bankflow.model.Account;
import com.bankflow.model.Customer;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customers", description = "Customer management and account operations")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final BankingService bankingService;

    @Operation(
            summary = "Update customer information",
            description = "Update customer details by username. Only provided fields will be updated."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{username}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @Parameter(description = "Username of the customer", required = true)
            @PathVariable String username,
            @Valid @RequestBody UpdateCustomerRequest request) {
        log.info("PUT /customers/{}", username);
        Customer updatedCustomer = bankingService.updateCustomer(username, request);
        return ResponseEntity.ok(updatedCustomer.mapToResponse());
    }

    @Operation(
            summary = "Get customer details",
            description = "Retrieve customer information by username"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{username}")
    public ResponseEntity<CustomerResponse> getCustomer(
            @Parameter(description = "Username of the customer", required = true)
            @PathVariable String username) {
        log.info("GET /customers/{} - Retrieving customer details by username", username);
        Customer customer = bankingService.getCustomerByUsername(username);
        return ResponseEntity.ok(customer.mapToResponse());
    }

    @Operation(
            summary = "List customer accounts",
            description = "Get all accounts associated with a customer by username"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of accounts retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{username}/accounts")
    public ResponseEntity<List<AccountResponse>> listCustomerAccounts(
            @Parameter(description = "Username of the customer", required = true)
            @PathVariable String username) {
        log.info("GET /customers/{}/accounts - Listing all accounts for customer", username);
        List<Account> accounts = bankingService.getCustomerAccountsByUsername(username);
        List<AccountResponse> responses = accounts.stream()
                .map(Account::mapToResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(
            summary = "Create new account",
            description = "Create a new account for a customer with an optional initial deposit"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{username}/accounts")
    public ResponseEntity<AccountResponse> createAccount(
            @Parameter(description = "Username of the customer", required = true)
            @PathVariable String username,
            @Valid @RequestBody CreateAccountRequest request) {
        log.info("POST /customers/{}/accounts - Creating new account", username);
        Account createdAccount = bankingService.createAccount(username, request.getInitialDeposit());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount.mapToResponse());
    }
}
