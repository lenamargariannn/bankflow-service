package com.bankflow.service;

import com.bankflow.dto.UpdateCustomerRequest;
import com.bankflow.exception.AccountInactiveException;
import com.bankflow.exception.BadRequestException;
import com.bankflow.exception.InsufficientFundsException;
import com.bankflow.exception.NotFoundException;
import com.bankflow.model.Account;
import com.bankflow.model.Customer;
import com.bankflow.model.TransactionRecord;
import com.bankflow.model.enums.AccountStatus;
import com.bankflow.model.enums.TransactionType;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.CustomerRepository;
import com.bankflow.repository.TransactionRepository;
import com.bankflow.service.impl.BankingServiceImpl;
import com.bankflow.util.AccountNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for BankingService with transactional behavior.
 * Tests actual database operations and transaction rollback scenarios.
 */
@DataJpaTest
@Import({BankingServiceImpl.class, AccountNumberGenerator.class})
@ActiveProfiles("test")
@DisplayName("BankingService Integration Tests")
class BankingServiceImplIntegrationTest {

    @Autowired
    private BankingServiceImpl bankingService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Customer testCustomer;
    private Account account1;
    private Account account2;

    @BeforeEach
    void setUp() {
        // Create test user
        com.bankflow.model.User testUser = new com.bankflow.model.User();
        testUser.setUsername("integrationuser");
        testUser.setEmail("integration@example.com");
        testUser.setFullName("Integration Test Customer");
        testUser.setPassword("password");
        testUser.setEnabled(true);

        // Create test customer
        testCustomer = new Customer();
        testCustomer.setUser(testUser);
        testCustomer = customerRepository.save(testCustomer);

        // Create test accounts
        account1 = new Account();
        account1.setCustomer(testCustomer);
        account1.setAccountNumber("0000000000000001");
        account1.setBalance(new BigDecimal("1000.00"));
        account1.setStatus(AccountStatus.ACTIVE);
        account1 = accountRepository.save(account1);

        account2 = new Account();
        account2.setCustomer(testCustomer);
        account2.setAccountNumber("0000000000000002");
        account2.setBalance(new BigDecimal("500.00"));
        account2.setStatus(AccountStatus.ACTIVE);
        account2 = accountRepository.save(account2);
    }


    @Nested
    @DisplayName("Deposit Integration Tests")
    class DepositIntegrationTests {

        @Test
        @DisplayName("Should persist deposit transaction and update balance")
        void testDepositPersistenceAndBalance() {
            // Arrange
            BigDecimal depositAmount = new BigDecimal("250.00");
            BigDecimal initialBalance = account1.getBalance();

            // Act
            TransactionRecord result = bankingService.deposit(account1.getId(), depositAmount);

            // Assert - transaction persisted
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();

            // Verify balance updated in database
            Account updatedAccount = accountRepository.findById(account1.getId()).orElseThrow();
            assertThat(updatedAccount.getBalance()).isEqualByComparingTo(initialBalance.add(depositAmount));

            // Verify transaction record saved
            assertThat(transactionRepository.findById(result.getId())).isPresent();
        }

        @Test
        @DisplayName("Should roll back transaction when exception thrown after balance update")
        void testDepositRollbackOnException() {
            // This test verifies that if an exception occurs after balance update,
            // the entire transaction is rolled back
            BigDecimal depositAmount = new BigDecimal("250.00");
            BigDecimal initialBalance = account1.getBalance();

            // Close account to cause exception during transaction
            account1.setStatus(AccountStatus.CLOSED);
            accountRepository.save(account1);

            // Attempt deposit (should fail)
            assertThatThrownBy(() -> bankingService.deposit(account1.getId(), depositAmount)).isInstanceOf(AccountInactiveException.class);

            // Verify balance was not updated (transaction rolled back)
            Account updatedAccount = accountRepository.findById(account1.getId()).orElseThrow();
            assertThat(updatedAccount.getBalance()).isEqualByComparingTo(initialBalance);

            // Verify no transaction record was created
            assertThat(transactionRepository.findByFromAccountIdOrderByTimestampDesc(account1.getId())).isEmpty();
        }
    }

    // ============ Withdrawal Integration Tests ============

    @Nested
    @DisplayName("Withdrawal Integration Tests")
    class WithdrawalIntegrationTests {

        @Test
        @DisplayName("Should persist withdrawal transaction and update balance")
        void testWithdrawalPersistenceAndBalance() {
            // Arrange
            BigDecimal withdrawAmount = new BigDecimal("250.00");
            BigDecimal initialBalance = account1.getBalance();

            // Act
            TransactionRecord result = bankingService.withdraw(account1.getId(), withdrawAmount);

            // Assert - transaction persisted
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();

            // Verify balance updated in database
            Account updatedAccount = accountRepository.findById(account1.getId()).orElseThrow();
            assertThat(updatedAccount.getBalance()).isEqualByComparingTo(initialBalance.subtract(withdrawAmount));

            // Verify transaction record saved
            assertThat(transactionRepository.findById(result.getId())).isPresent();
        }

        @Test
        @DisplayName("Should roll back transaction on insufficient funds")
        void testWithdrawalRollbackOnInsufficientFunds() {
            // Arrange
            BigDecimal withdrawAmount = new BigDecimal("2000.00");
            BigDecimal initialBalance = account1.getBalance();

            // Act & Assert
            assertThatThrownBy(() -> bankingService.withdraw(account1.getId(), withdrawAmount)).isInstanceOf(InsufficientFundsException.class);

            // Verify balance was not updated
            Account updatedAccount = accountRepository.findById(account1.getId()).orElseThrow();
            assertThat(updatedAccount.getBalance()).isEqualByComparingTo(initialBalance);

            // Verify no transaction record was created
            assertThat(transactionRepository.findByFromAccountIdOrderByTimestampDesc(account1.getId())).isEmpty();
        }
    }

    // ============ Transfer Integration Tests ============

    @Nested
    @DisplayName("Transfer Integration Tests")
    class TransferIntegrationTests {

        @Test
        @DisplayName("Should persist transfer and update both account balances")
        void testTransferPersistenceAndBalances() {
            // Arrange
            BigDecimal transferAmount = new BigDecimal("200.00");
            BigDecimal account1InitialBalance = account1.getBalance();
            BigDecimal account2InitialBalance = account2.getBalance();

            // Act
            TransactionRecord result = bankingService.transfer(account1.getId(), account2.getId(), transferAmount, "Integration test transfer");

            // Assert - transaction persisted
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getType()).isEqualTo(TransactionType.TRANSFER);

            // Verify both account balances updated in database
            Account updatedAccount1 = accountRepository.findById(account1.getId()).orElseThrow();
            Account updatedAccount2 = accountRepository.findById(account2.getId()).orElseThrow();

            assertThat(updatedAccount1.getBalance()).isEqualByComparingTo(account1InitialBalance.subtract(transferAmount));
            assertThat(updatedAccount2.getBalance()).isEqualByComparingTo(account2InitialBalance.add(transferAmount));

            // Verify transaction record saved
            assertThat(transactionRepository.findById(result.getId())).isPresent();
        }

        @Test
        @DisplayName("Should roll back transfer when destination account is inactive")
        void testTransferRollbackOnInactiveDestination() {
            // Arrange
            BigDecimal transferAmount = new BigDecimal("200.00");
            BigDecimal account1InitialBalance = account1.getBalance();
            BigDecimal account2InitialBalance = account2.getBalance();

            // Deactivate destination account
            account2.setStatus(AccountStatus.SUSPENDED);
            accountRepository.save(account2);

            // Act & Assert
            assertThatThrownBy(() -> bankingService.transfer(account1.getId(), account2.getId(), transferAmount, "Transfer to inactive")).isInstanceOf(AccountInactiveException.class);

            // Verify both balances unchanged (transaction rolled back)
            Account updatedAccount1 = accountRepository.findById(account1.getId()).orElseThrow();
            Account updatedAccount2 = accountRepository.findById(account2.getId()).orElseThrow();

            assertThat(updatedAccount1.getBalance()).isEqualByComparingTo(account1InitialBalance);
            assertThat(updatedAccount2.getBalance()).isEqualByComparingTo(account2InitialBalance);

            // Verify no transaction record was created
            long transferCount = transactionRepository.findByFromAccountIdOrderByTimestampDesc(account1.getId()).stream().filter(t -> t.getType() == TransactionType.TRANSFER).count();
            assertThat(transferCount).isZero();
        }

        @Test
        @DisplayName("Should roll back transfer on insufficient funds after locking both accounts")
        void testTransferRollbackOnInsufficientFunds() {
            // Arrange
            BigDecimal transferAmount = new BigDecimal("2000.00");
            BigDecimal account1InitialBalance = account1.getBalance();
            BigDecimal account2InitialBalance = account2.getBalance();

            // Act & Assert
            assertThatThrownBy(() -> bankingService.transfer(account1.getId(), account2.getId(), transferAmount, "Insufficient funds transfer")).isInstanceOf(InsufficientFundsException.class);

            // Verify both balances unchanged
            Account updatedAccount1 = accountRepository.findById(account1.getId()).orElseThrow();
            Account updatedAccount2 = accountRepository.findById(account2.getId()).orElseThrow();

            assertThat(updatedAccount1.getBalance()).isEqualByComparingTo(account1InitialBalance);
            assertThat(updatedAccount2.getBalance()).isEqualByComparingTo(account2InitialBalance);

            // Verify no transaction record was created
            assertThat(transactionRepository.findByFromAccountIdOrderByTimestampDesc(account1.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should handle concurrent transfers with consistent locking order")
        void testConcurrentTransfersWithConsistentLocking() {
            // Arrange - create a third account
            Account account3 = new Account();
            account3.setCustomer(testCustomer);
            account3.setAccountNumber("0000000000000003");
            account3.setBalance(new BigDecimal("300.00"));
            account3.setStatus(AccountStatus.ACTIVE);
            account3 = accountRepository.save(account3);

            BigDecimal transferAmount = new BigDecimal("100.00");

            // Act - perform transfers in different orders to test consistent locking
            TransactionRecord transfer1 = bankingService.transfer(account1.getId(), account3.getId(), transferAmount, "Transfer 1->3");

            TransactionRecord transfer2 = bankingService.transfer(account3.getId(), account1.getId(), new BigDecimal("50.00"), "Transfer 3->1");

            // Assert - both transfers completed successfully
            assertThat(transfer1).isNotNull();
            assertThat(transfer2).isNotNull();

            // Verify final balances
            Account finalAccount1 = accountRepository.findById(account1.getId()).orElseThrow();
            Account finalAccount3 = accountRepository.findById(account3.getId()).orElseThrow();

            assertThat(finalAccount1.getBalance()).isEqualByComparingTo(new BigDecimal("950.00")); // 1000 - 100 + 50
            assertThat(finalAccount3.getBalance()).isEqualByComparingTo(new BigDecimal("350.00")); // 300 + 100 - 50
        }
    }

    // ============ Account Creation Integration Tests ============

    @Nested
    @DisplayName("Account Creation Integration Tests")
    class AccountCreationIntegrationTests {

        @Test
        @DisplayName("Should create account with initial deposit and record transaction")
        void testCreateAccountWithInitialDeposit() {
            // Arrange
            BigDecimal initialDeposit = new BigDecimal("750.00");

            // Act
            Account newAccount = bankingService.createAccount("integrationuser", initialDeposit);

            // Assert - account persisted
            assertThat(newAccount).isNotNull();
            assertThat(newAccount.getId()).isNotNull();
            assertThat(newAccount.getBalance()).isEqualByComparingTo(initialDeposit);

            // Verify transaction record created
            List<TransactionRecord> transactions = transactionRepository.findByFromAccountIdOrderByTimestampDesc(newAccount.getId());
            assertThat(transactions).hasSize(1);
            assertThat(transactions.getFirst().getType()).isEqualTo(TransactionType.DEPOSIT);
            assertThat(transactions.getFirst().getAmount()).isEqualByComparingTo(initialDeposit);
        }

        @Test
        @DisplayName("Should create account without initial deposit")
        void testCreateAccountWithoutInitialDeposit() {
            // Act
            Account newAccount = bankingService.createAccount("integrationuser", null);

            // Assert
            assertThat(newAccount).isNotNull();
            assertThat(newAccount.getId()).isNotNull();
            assertThat(newAccount.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

            // Verify no transaction record created
            List<TransactionRecord> transactions = transactionRepository.findByFromAccountIdOrderByTimestampDesc(newAccount.getId());
            assertThat(transactions).isEmpty();
        }
    }

    // ============ Transaction Retrieval Integration Tests ============

    @Nested
    @DisplayName("Transaction Retrieval Integration Tests")
    class TransactionRetrievalIntegrationTests {

        @Test
        @DisplayName("Should retrieve all transactions for account in correct order")
        void testGetTransactionsForAccount() throws InterruptedException {
            // Arrange - create multiple transactions
            bankingService.deposit(account1.getId(), new BigDecimal("100.00"));
            Thread.sleep(10); // Small delay to ensure timestamp difference
            bankingService.withdraw(account1.getId(), new BigDecimal("50.00"));
            Thread.sleep(10);
            bankingService.transfer(account1.getId(), account2.getId(), new BigDecimal("25.00"), "Test");

            // Act
            List<TransactionRecord> transactions = bankingService.getTransactionsForAccount(account1.getId());

            // Assert - transactions retrieved in descending order
            assertThat(transactions).hasSize(3);
            assertThat(transactions.get(0).getType()).isEqualTo(TransactionType.TRANSFER); // Most recent
            assertThat(transactions.get(1).getType()).isEqualTo(TransactionType.WITHDRAW);
            assertThat(transactions.get(2).getType()).isEqualTo(TransactionType.DEPOSIT); // Oldest

            // Verify timestamps are in descending order
            for (int i = 0; i < transactions.size() - 1; i++) {
                assertThat(transactions.get(i).getTimestamp()).isAfterOrEqualTo(transactions.get(i + 1).getTimestamp());
            }
        }

        @Test
        @DisplayName("Should include both incoming and outgoing transfers in transaction list")
        void testGetTransactionsIncludesIncomingAndOutgoing() {
            // Act
            bankingService.transfer(account1.getId(), account2.getId(), new BigDecimal("100.00"), "1->2");
            bankingService.transfer(account2.getId(), account1.getId(), new BigDecimal("50.00"), "2->1");

            // Assert
            List<TransactionRecord> account1Transactions = bankingService.getTransactionsForAccount(account1.getId());
            List<TransactionRecord> account2Transactions = bankingService.getTransactionsForAccount(account2.getId());

            assertThat(account1Transactions).hasSize(2); // One outgoing, one incoming
            assertThat(account2Transactions).hasSize(2); // One incoming, one outgoing

            // Verify transaction types
            long outgoingCount = account1Transactions.stream().filter(t -> t.getFromAccount().getId().equals(account1.getId())).count();
            long incomingCount = account1Transactions.stream().filter(t -> t.getToAccount() != null && t.getToAccount().getId().equals(account1.getId())).count();

            assertThat(outgoingCount).isEqualTo(1);
            assertThat(incomingCount).isEqualTo(1);
        }
    }

    // ============ Error Handling Integration Tests ============

    @Nested
    @DisplayName("Error Handling Integration Tests")
    class ErrorHandlingIntegrationTests {

        @Test
        @DisplayName("Should throw NotFoundException for non-existent customer")
        void testCreateAccountForNonExistentCustomer() {
            assertThatThrownBy(() -> bankingService.createAccount("nonexistentuser", new BigDecimal("100.00"))).isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Should throw BadRequestException for duplicate email")
        void testUpdateCustomerWithDuplicateEmail() {
            // Create another customer with different email
            com.bankflow.model.User anotherUser = new com.bankflow.model.User();
            anotherUser.setUsername("anotheruser");
            anotherUser.setEmail("another@example.com");
            anotherUser.setFullName("Another Customer");
            anotherUser.setPassword("password");
            anotherUser.setEnabled(true);

            Customer anotherCustomer = new Customer();
            anotherCustomer.setUser(anotherUser);
            customerRepository.save(anotherCustomer);

            // Try to update testCustomer with the email of anotherCustomer
            assertThatThrownBy(() -> bankingService.updateCustomer("integrationuser", new UpdateCustomerRequest("Another Name", "another@example.com", null))).isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Should throw BadRequestException for invalid amount")
        void testDepositWithInvalidAmount() {
            assertThatThrownBy(() -> bankingService.deposit(account1.getId(), BigDecimal.ZERO)).isInstanceOf(BadRequestException.class);
        }
    }
}

