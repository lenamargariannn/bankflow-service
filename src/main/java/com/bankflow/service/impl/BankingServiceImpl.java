package com.bankflow.service.impl;

import com.bankflow.dto.UpdateCustomerRequest;
import com.bankflow.exception.AccountInactiveException;
import com.bankflow.exception.BadRequestException;
import com.bankflow.exception.InsufficientFundsException;
import com.bankflow.exception.NotFoundException;
import com.bankflow.model.Account;
import com.bankflow.model.Customer;
import com.bankflow.model.TransactionRecord;
import com.bankflow.model.User;
import com.bankflow.model.enums.AccountStatus;
import com.bankflow.model.enums.TransactionType;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.CustomerRepository;
import com.bankflow.repository.TransactionRepository;
import com.bankflow.service.BankingService;
import com.bankflow.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BankingServiceImpl implements BankingService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    @Override
    public Customer updateCustomer(String username, UpdateCustomerRequest request) {
        log.info("AUDIT: Updating customer - Username: {}", username);

        Customer customer = customerRepository.findByUser_Username(username).orElseThrow(() -> {
            log.error("AUDIT: Update failed - Customer not found. Username: {}", username);
            return new NotFoundException("Customer with username", username);
        });

        User user = customer.getUser();
        String email = request.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            if (!user.getEmail().equals(email)) {
                customerRepository.findByUser_Email(email).ifPresent(existingCustomer -> {
                    if (!existingCustomer.getUser().getUsername().equals(username)) {
                        log.warn("AUDIT: Customer update rejected - Email already exists: {}", email);
                        throw new BadRequestException("email", email, "Email already exists for another customer");
                    }
                });
                user.setEmail(email);
            }
        }
        String fullName = request.getFullName();
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        }

        String phoneNumber = request.getPhoneNumber();
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            customerRepository.findByPhoneNumber(phoneNumber).ifPresent(existingCustomer -> {
                if (!existingCustomer.getUser().getUsername().equals(username)) {
                    log.warn("AUDIT: Customer update rejected - Phone number already exists: {}", phoneNumber);
                    throw new BadRequestException("phoneNumber", phoneNumber, "Phone number already exists for another customer");
                }
            });
            customer.setPhoneNumber(phoneNumber);
        }
        Customer savedCustomer = customerRepository.save(customer);
        log.info("AUDIT: Customer updated successfully - Username: {}, Email: {}", username, user.getEmail());

        return savedCustomer;
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomer(Long customerId) {
        log.info("Fetching customer with ID: {}", customerId);
        return customerRepository.findById(customerId).orElseThrow(() -> new NotFoundException("Customer", customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerByUsername(String username) {
        log.info("Fetching customer with username: {}", username);
        return customerRepository.findByUser_Username(username).orElseThrow(() -> {
            log.error("Customer not found with username: {}", username);
            return new NotFoundException("Customer with username", username);
        });
    }

    @Override
    public Account createAccount(String username, BigDecimal initialDeposit) {
        log.info("AUDIT: Creating account - Username: {}, Initial Deposit: {}", username, initialDeposit);
        if (initialDeposit != null && initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("AUDIT: Account creation rejected - Negative initial deposit: {}", initialDeposit);
            throw new BadRequestException("initialDeposit", initialDeposit.toString(), "Cannot be negative");
        }

        Customer customer = getCustomerByUsername(username);

        accountRepository.lockAccountsTable();
        log.debug("AUDIT: Acquired lock on accounts table for account number generation");

        String accountNumber = generateUniqueAccountNumber();

        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountNumber(accountNumber);
        account.setBalance(initialDeposit != null ? initialDeposit : BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);

        Account savedAccount = accountRepository.save(account);
        log.info("AUDIT: Account created successfully - Account ID: {}, Account Number: {}, Username: {}, Initial Balance: {}", savedAccount.getId(), savedAccount.getAccountNumber(), username, savedAccount.getBalance());

        if (initialDeposit != null && initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            TransactionRecord transaction = new TransactionRecord();
            transaction.setType(TransactionType.DEPOSIT);
            transaction.setAmount(initialDeposit);
            transaction.setFromAccount(savedAccount);
            transaction.setDescription("Initial deposit");
            transaction.setTimestamp(LocalDateTime.now());
            TransactionRecord savedTransaction = transactionRepository.save(transaction);
            log.info("AUDIT: Initial deposit transaction recorded - Transaction ID: {}, Account ID: {}, Amount: {}", savedTransaction.getId(), savedAccount.getId(), initialDeposit);
        }

        return savedAccount;
    }

    private String generateUniqueAccountNumber() {
        int maxAttempts = 10;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String accountNumber = accountNumberGenerator.generateAccountNumber();

            if (!accountRepository.existsByAccountNumber(accountNumber)) {
                log.debug("AUDIT: Generated unique account number: {} on attempt {}", accountNumber, attempt);
                return accountNumber;
            }

            log.warn("AUDIT: Account number collision detected: {}. Regenerating... Attempt {}/{}", accountNumber, attempt, maxAttempts);
        }

        log.error("AUDIT: Failed to generate unique account number after {} attempts", maxAttempts);
        throw new RuntimeException("Unable to generate unique account number after " + maxAttempts + " attempts. Please try again.");
    }

    @Override
    @Transactional(readOnly = true)
    public void getAccount(Long accountId) {
        log.info("Fetching account with ID: {}", accountId);
        accountRepository.findById(accountId).orElseThrow(() -> new NotFoundException("Account", accountId));
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccountByNumber(String accountNumber) {
        log.info("Fetching account with number: {}", accountNumber);
        return accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new NotFoundException("Account with number", accountNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getCustomerAccountsByUsername(String username) {
        log.info("Fetching accounts for username: {}", username);
        Customer customer = getCustomerByUsername(username);
        return accountRepository.findByCustomerId(customer.getId());
    }

    @Override
    public TransactionRecord deposit(Long accountId, BigDecimal amount) {
        log.info("AUDIT: Initiating deposit - Account ID: {}, Amount: {}", accountId, amount);

        validateAmount(amount);

        Account account = accountRepository.findByIdForUpdate(accountId).orElseThrow(() -> {
            log.error("AUDIT: Deposit failed - Account not found. Account ID: {}", accountId);
            return new NotFoundException("Account", accountId);
        });

        if (account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("AUDIT: Deposit rejected - Account inactive. Account ID: {}, Status: {}", accountId, account.getStatus());
            throw new AccountInactiveException(accountId, account.getStatus());
        }

        BigDecimal oldBalance = account.getBalance();
        BigDecimal newBalance = oldBalance.add(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);
        log.debug("AUDIT: Balance updated - Account ID: {}, Old Balance: {}, New Balance: {}, Deposit Amount: {}", accountId, oldBalance, newBalance, amount);

        TransactionRecord transaction = new TransactionRecord();
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setFromAccount(account);
        transaction.setDescription("Deposit");
        transaction.setTimestamp(LocalDateTime.now());
        TransactionRecord savedTransaction = transactionRepository.save(transaction);

        log.info("AUDIT: Deposit completed successfully - Transaction ID: {}, Account ID: {}, Amount: {}, New Balance: {}", savedTransaction.getId(), accountId, amount, newBalance);

        return savedTransaction;
    }

    @Override
    public TransactionRecord depositByAccountNumber(String accountNumber, BigDecimal amount) {
        log.info("AUDIT: Initiating deposit - Account Number: {}, Amount: {}", accountNumber, amount);
        Account account = getAccountByNumber(accountNumber);
        return deposit(account.getId(), amount);
    }

    @Override
    public TransactionRecord withdraw(Long accountId, BigDecimal amount) {
        log.info("AUDIT: Initiating withdrawal - Account ID: {}, Amount: {}", accountId, amount);

        validateAmount(amount);

        Account account = accountRepository.findByIdForUpdate(accountId).orElseThrow(() -> {
            log.error("AUDIT: Withdrawal failed - Account not found. Account ID: {}", accountId);
            return new NotFoundException("Account", accountId);
        });

        if (account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("AUDIT: Withdrawal rejected - Account inactive. Account ID: {}, Status: {}", accountId, account.getStatus());
            throw new AccountInactiveException(accountId, account.getStatus());
        }

        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("AUDIT: Withdrawal rejected - Insufficient funds. Account ID: {}, Required: {}, Available: {}", accountId, amount, account.getBalance());
            throw new InsufficientFundsException(accountId, amount, account.getBalance());
        }

        BigDecimal oldBalance = account.getBalance();
        BigDecimal newBalance = oldBalance.subtract(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);
        log.debug("AUDIT: Balance updated - Account ID: {}, Old Balance: {}, New Balance: {}, Withdrawal Amount: {}", accountId, oldBalance, newBalance, amount);

        TransactionRecord transaction = new TransactionRecord();
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setAmount(amount);
        transaction.setFromAccount(account);
        transaction.setDescription("Withdrawal");
        transaction.setTimestamp(LocalDateTime.now());
        TransactionRecord savedTransaction = transactionRepository.save(transaction);

        log.info("AUDIT: Withdrawal completed successfully - Transaction ID: {}, Account ID: {}, Amount: {}, New Balance: {}", savedTransaction.getId(), accountId, amount, newBalance);

        return savedTransaction;
    }

    @Override
    public TransactionRecord withdrawByAccountNumber(String accountNumber, BigDecimal amount) {
        log.info("AUDIT: Initiating withdrawal - Account Number: {}, Amount: {}", accountNumber, amount);
        Account account = getAccountByNumber(accountNumber);
        return withdraw(account.getId(), amount);
    }

    @Override
    @Transactional
    public TransactionRecord transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String description) {
        log.info("AUDIT: Initiating transfer - From Account ID: {}, To Account ID: {}, Amount: {}, Description: {}", fromAccountId, toAccountId, amount, description);

        validateAmount(amount);

        if (fromAccountId.equals(toAccountId)) {
            log.warn("AUDIT: Transfer rejected - Self-transfer attempted. Account ID: {}", fromAccountId);
            throw new BadRequestException("toAccountId", toAccountId.toString(), "Cannot transfer to same account");
        }

        Long firstLockId = fromAccountId < toAccountId ? fromAccountId : toAccountId;
        Long secondLockId = fromAccountId < toAccountId ? toAccountId : fromAccountId;

        Account firstLocked = accountRepository.findByIdForUpdate(firstLockId).orElseThrow(() -> {
            log.error("AUDIT: Transfer failed - Account not found. Account ID: {}", firstLockId);
            return new NotFoundException("Account", firstLockId);
        });

        Account secondLocked = accountRepository.findByIdForUpdate(secondLockId).orElseThrow(() -> {
            log.error("AUDIT: Transfer failed - Account not found. Account ID: {}", secondLockId);
            return new NotFoundException("Account", secondLockId);
        });

        Account fromAccount = fromAccountId.equals(firstLocked.getId()) ? firstLocked : secondLocked;
        Account toAccount = toAccountId.equals(firstLocked.getId()) ? firstLocked : secondLocked;

        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            log.warn("AUDIT: Transfer rejected - Source account inactive. Account ID: {}, Status: {}", fromAccountId, fromAccount.getStatus());
            throw new AccountInactiveException(fromAccountId, fromAccount.getStatus());
        }

        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            log.warn("AUDIT: Transfer rejected - Destination account inactive. Account ID: {}, Status: {}", toAccountId, toAccount.getStatus());
            throw new AccountInactiveException(toAccountId, toAccount.getStatus());
        }

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            log.warn("AUDIT: Transfer rejected - Insufficient funds. From Account ID: {}, Required: {}, Available: {}", fromAccountId, amount, fromAccount.getBalance());
            throw new InsufficientFundsException(fromAccountId, amount, fromAccount.getBalance());
        }

        BigDecimal fromOldBalance = fromAccount.getBalance();
        BigDecimal toOldBalance = toAccount.getBalance();

        fromAccount.setBalance(fromOldBalance.subtract(amount));
        toAccount.setBalance(toOldBalance.add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        TransactionRecord transaction = new TransactionRecord();
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setDescription(description != null ? description : "Transfer");
        transaction.setTimestamp(LocalDateTime.now());

        TransactionRecord savedTransaction = transactionRepository.save(transaction);

        log.info("AUDIT: Transfer completed successfully - Transaction ID: {}, From Account ID: {}, To Account ID: {}, Amount: {}", savedTransaction.getId(), fromAccountId, toAccountId, amount);

        return savedTransaction;
    }

    @Override
    @Transactional
    public TransactionRecord transferByAccountNumber(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description) {
        log.info("AUDIT: Initiating transfer - From Account Number: {}, To Account Number: {}, Amount: {}, Description: {}", fromAccountNumber, toAccountNumber, amount, description);
        Account fromAccount = getAccountByNumber(fromAccountNumber);
        Account toAccount = getAccountByNumber(toAccountNumber);
        return transfer(fromAccount.getId(), toAccount.getId(), amount, description);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionRecord> getTransactionsForAccount(Long accountId) {
        log.info("Fetching transactions for account ID: {}", accountId);
        getAccount(accountId);
        return transactionRepository.findByFromAccountIdOrToAccountIdOrderByTimestampDesc(accountId, accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionRecord getTransactionForAccount(Long accountId, Long transactionId) {
        log.info("Fetching transaction ID: {} for account ID: {}", transactionId, accountId);

        getAccount(accountId);

        TransactionRecord transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new NotFoundException("Transaction", transactionId));

        boolean belongsToAccount = (transaction.getFromAccount() != null && transaction.getFromAccount().getId().equals(accountId)) || (transaction.getToAccount() != null && transaction.getToAccount().getId().equals(accountId));

        if (!belongsToAccount) {
            log.warn("AUDIT: Transaction does not belong to account. Transaction ID: {}, Account ID: {}", transactionId, accountId);
            throw new BadRequestException("transactionId", transactionId.toString(), "Transaction does not belong to this account");
        }

        return transaction;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionRecord> getTransactionsByAccountNumber(String accountNumber) {
        log.info("Fetching transactions for account number: {}", accountNumber);
        Account account = getAccountByNumber(accountNumber);
        getAccount(account.getId());
        return transactionRepository.findByFromAccountIdOrToAccountIdOrderByTimestampDesc(account.getId(), account.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionRecord getTransactionForAccountByNumber(String accountNumber, Long transactionId) {
        log.info("Fetching transaction ID: {} for account number: {}", transactionId, accountNumber);
        Account account = getAccountByNumber(accountNumber);
        return getTransactionForAccount(account.getId(), transactionId);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BadRequestException("amount", "null", "Amount cannot be null");
        }
        if (amount.compareTo(new BigDecimal("0.01")) < 0) {
            throw new BadRequestException("amount", amount.toString(), "Amount must be greater than 0");
        }
    }
}
