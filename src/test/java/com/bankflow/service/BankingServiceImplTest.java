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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BankingService.
 * Uses Mockito for repository mocking.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BankingService Unit Tests")
class BankingServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @InjectMocks
    private BankingServiceImpl bankingService;

    private Customer testCustomer;
    private Account testAccount;
    private Account testAccount2;

    @BeforeEach
    void setUp() {
        com.bankflow.model.User testUser = new com.bankflow.model.User();
        testUser.setId(1L);
        testUser.setUsername("john@example.com");
        testUser.setEmail("john@example.com");
        testUser.setFullName("John Doe");
        testUser.setPassword("password");
        testUser.setEnabled(true);

        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setUser(testUser);
        testCustomer.setCreatedAt(LocalDateTime.now());
        testCustomer.setUpdatedAt(LocalDateTime.now());

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setAccountNumber("0000000000001");
        testAccount.setCustomer(testCustomer);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setVersion(0L);
        testAccount.setCreatedAt(LocalDateTime.now());
        testAccount.setUpdatedAt(LocalDateTime.now());

        testAccount2 = new Account();
        testAccount2.setId(2L);
        testAccount2.setAccountNumber("0000000000002");
        testAccount2.setCustomer(testCustomer);
        testAccount2.setBalance(new BigDecimal("500.00"));
        testAccount2.setStatus(AccountStatus.ACTIVE);
        testAccount2.setVersion(0L);
        testAccount2.setCreatedAt(LocalDateTime.now());
        testAccount2.setUpdatedAt(LocalDateTime.now());
    }


    @Nested
    @DisplayName("Customer Update Tests")
    class CustomerUpdateTests {

        @Test
        @DisplayName("Should update customer successfully")
        void testUpdateCustomer() {
            // Arrange
            String username = "testuser";
            String fullName = "Jane Smith";
            String email = "jane@example.com";
            when(customerRepository.findByUser_Username(username)).thenReturn(Optional.of(testCustomer));
            when(customerRepository.findByUser_Email(email)).thenReturn(Optional.empty());
            when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

            // Act
            Customer result = bankingService.updateCustomer(username, new UpdateCustomerRequest(fullName, email, null));

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(customerRepository).findByUser_Username(username);
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException for duplicate email")
        void testUpdateCustomerWithDuplicateEmail() {
            // Arrange
            String username = "testuser";
            String email = "jane@example.com";
            Customer otherCustomer = new Customer();
            otherCustomer.setId(2L);
            com.bankflow.model.User otherUser = new com.bankflow.model.User();
            otherUser.setUsername("otheruser");
            otherUser.setEmail(email);
            otherCustomer.setUser(otherUser);

            when(customerRepository.findByUser_Username(username)).thenReturn(Optional.of(testCustomer));
            when(customerRepository.findByUser_Email(email)).thenReturn(Optional.of(otherCustomer));

            // Act & Assert
            assertThatThrownBy(() -> bankingService.updateCustomer(username, new UpdateCustomerRequest("John Doe", email, null))).isInstanceOf(BadRequestException.class).hasMessageContaining("already exists");
            verify(customerRepository).findByUser_Username(username);
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException for non-existent customer")
        void testUpdateCustomerNotFound() {
            // Arrange
            String username = "nonexistentuser";
            when(customerRepository.findByUser_Username(username)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bankingService.updateCustomer(username, new UpdateCustomerRequest("John Doe", "john@example.com", null))).isInstanceOf(NotFoundException.class);
            verify(customerRepository).findByUser_Username(username);
            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Customer Retrieval Tests")
    class CustomerRetrievalTests {

        @Test
        @DisplayName("Should get customer by ID")
        void testGetCustomer() {
            // Arrange
            when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

            // Act
            Customer result = bankingService.getCustomer(1L);

            // Assert
            assertThat(result).isEqualTo(testCustomer);
            verify(customerRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw NotFoundException for non-existent customer")
        void testGetCustomerNotFound() {
            // Arrange
            when(customerRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bankingService.getCustomer(999L)).isInstanceOf(NotFoundException.class);
            verify(customerRepository).findById(999L);
        }
    }

    // ============ Account Operations Tests ============

    @Nested
    @DisplayName("Account Creation Tests")
    class AccountCreationTests {

        @Test
        @DisplayName("Should create account without initial deposit")
        void testCreateAccountWithoutDeposit() {
            // Arrange
            String username = "testuser";
            when(customerRepository.findByUser_Username(username)).thenReturn(Optional.of(new Customer()));
            when(accountRepository.save(any(Account.class))).thenReturn(new Account(1L, "0000000000001", testCustomer, BigDecimal.ZERO, AccountStatus.ACTIVE, 0L, LocalDateTime.now(), LocalDateTime.now()));

            // Act
            Account result = bankingService.createAccount(username, BigDecimal.ZERO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            verify(customerRepository).findByUser_Username(username);
            verify(accountRepository).save(any(Account.class));
        }

        @Test
        @DisplayName("Should create account with initial deposit")
        void testCreateAccountWithDeposit() {
            // Arrange
            String username = "testuser";
            BigDecimal deposit = new BigDecimal("500.00");
            Account accountWithDeposit = new Account();
            accountWithDeposit.setId(1L);
            accountWithDeposit.setBalance(deposit);
            accountWithDeposit.setStatus(AccountStatus.ACTIVE);
            accountWithDeposit.setCustomer(testCustomer);

            when(customerRepository.findByUser_Username(username)).thenReturn(Optional.of(testCustomer));
            when(accountRepository.save(any(Account.class))).thenReturn(accountWithDeposit);
            when(transactionRepository.save(any(TransactionRecord.class))).thenReturn(new TransactionRecord());

            // Act
            Account result = bankingService.createAccount(username, deposit);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getBalance()).isEqualByComparingTo(deposit);
            verify(accountRepository).save(any(Account.class));
            verify(transactionRepository).save(any(TransactionRecord.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException for negative initial deposit")
        void testCreateAccountWithNegativeDeposit() {
            // Arrange
            String username = "testuser";
            BigDecimal negativeDeposit = new BigDecimal("-100.00");

            // Act & Assert
            assertThatThrownBy(() -> bankingService.createAccount(username, negativeDeposit)).isInstanceOf(BadRequestException.class);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException for non-existent customer")
        void testCreateAccountForNonExistentCustomer() {
            // Arrange
            String username = "nonexistentuser";
            when(customerRepository.findByUser_Username(username)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bankingService.createAccount(username, new BigDecimal("100.00"))).isInstanceOf(NotFoundException.class);
            verify(accountRepository, never()).save(any());
        }
    }

    // ============ Deposit Tests ============

    @Nested
    @DisplayName("Deposit Tests")
    class DepositTests {

        @Test
        @DisplayName("Should deposit money successfully")
        void testDepositSuccessful() {
            // Arrange
            BigDecimal depositAmount = new BigDecimal("100.00");
            Account updatedAccount = new Account();
            updatedAccount.setId(1L);
            updatedAccount.setBalance(new BigDecimal("1100.00"));
            updatedAccount.setStatus(AccountStatus.ACTIVE);
            updatedAccount.setCustomer(testCustomer);

            when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);
            when(transactionRepository.save(any(TransactionRecord.class))).thenReturn(new TransactionRecord(1L, TransactionType.DEPOSIT, depositAmount, testAccount, null, LocalDateTime.now(), null, null, LocalDateTime.now()));

            // Act
            TransactionRecord result = bankingService.deposit(1L, depositAmount);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(TransactionType.DEPOSIT);
            verify(accountRepository).findByIdForUpdate(1L);
            verify(accountRepository).save(any(Account.class));
            verify(transactionRepository).save(any(TransactionRecord.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException for zero amount")
        void testDepositZeroAmount() {
            // Arrange
            BigDecimal zeroAmount = BigDecimal.ZERO;

            // Act & Assert
            assertThatThrownBy(() -> bankingService.deposit(1L, zeroAmount)).isInstanceOf(BadRequestException.class);
            verify(accountRepository, never()).findByIdForUpdate(anyLong());
        }

        @Test
        @DisplayName("Should throw AccountInactiveException for inactive account")
        void testDepositToInactiveAccount() {
            // Arrange
            Account inactiveAccount = new Account();
            inactiveAccount.setId(1L);
            inactiveAccount.setStatus(AccountStatus.SUSPENDED);
            when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(inactiveAccount));

            // Act & Assert
            assertThatThrownBy(() -> bankingService.deposit(1L, new BigDecimal("100.00"))).isInstanceOf(AccountInactiveException.class);
            verify(accountRepository).findByIdForUpdate(1L);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException for non-existent account")
        void testDepositToNonExistentAccount() {
            // Arrange
            when(accountRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bankingService.deposit(999L, new BigDecimal("100.00"))).isInstanceOf(NotFoundException.class);
            verify(transactionRepository, never()).save(any());
        }
    }

    // ============ Withdrawal Tests ============

    @Nested
    @DisplayName("Withdrawal Tests")
    class WithdrawalTests {

        @Test
        @DisplayName("Should withdraw money successfully")
        void testWithdrawSuccessful() {
            // Arrange
            BigDecimal withdrawAmount = new BigDecimal("100.00");
            Account updatedAccount = new Account();
            updatedAccount.setId(1L);
            updatedAccount.setBalance(new BigDecimal("900.00"));
            updatedAccount.setStatus(AccountStatus.ACTIVE);
            updatedAccount.setCustomer(testCustomer);

            when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);
            when(transactionRepository.save(any(TransactionRecord.class))).thenReturn(new TransactionRecord(1L, TransactionType.WITHDRAW, withdrawAmount, testAccount, null, LocalDateTime.now(), null, null, LocalDateTime.now()));

            // Act
            TransactionRecord result = bankingService.withdraw(1L, withdrawAmount);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(TransactionType.WITHDRAW);
            verify(accountRepository).findByIdForUpdate(1L);
            verify(accountRepository).save(any(Account.class));
            verify(transactionRepository).save(any(TransactionRecord.class));
        }

        @Test
        @DisplayName("Should throw InsufficientFundsException when balance is insufficient")
        void testWithdrawInsufficientFunds() {
            // Arrange
            BigDecimal withdrawAmount = new BigDecimal("2000.00");
            when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testAccount));

            // Act & Assert
            assertThatThrownBy(() -> bankingService.withdraw(1L, withdrawAmount)).isInstanceOf(InsufficientFundsException.class);
            verify(accountRepository, never()).save(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AccountInactiveException for inactive account")
        void testWithdrawFromInactiveAccount() {
            // Arrange
            Account inactiveAccount = new Account();
            inactiveAccount.setId(1L);
            inactiveAccount.setStatus(AccountStatus.CLOSED);
            when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(inactiveAccount));

            // Act & Assert
            assertThatThrownBy(() -> bankingService.withdraw(1L, new BigDecimal("100.00"))).isInstanceOf(AccountInactiveException.class);
            verify(accountRepository, never()).save(any());
        }
    }

    // ============ Transfer Tests ============

    @Nested
    @DisplayName("Transfer Tests")
    class TransferTests {

        @Test
        @DisplayName("Should transfer money successfully")
        void testTransferSuccessful() {
            // Arrange
            BigDecimal transferAmount = new BigDecimal("100.00");
            Account fromAccountUpdated = new Account();
            fromAccountUpdated.setId(1L);
            fromAccountUpdated.setBalance(new BigDecimal("900.00"));
            fromAccountUpdated.setStatus(AccountStatus.ACTIVE);
            fromAccountUpdated.setCustomer(testCustomer);

            Account toAccountUpdated = new Account();
            toAccountUpdated.setId(2L);
            toAccountUpdated.setBalance(new BigDecimal("600.00"));
            toAccountUpdated.setStatus(AccountStatus.ACTIVE);
            toAccountUpdated.setCustomer(testCustomer);

            when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testAccount));
            when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(testAccount2));
            when(accountRepository.save(any(Account.class))).thenReturn(fromAccountUpdated).thenReturn(toAccountUpdated);
            when(transactionRepository.save(any(TransactionRecord.class))).thenReturn(new TransactionRecord(1L, TransactionType.TRANSFER, transferAmount, testAccount, testAccount2, LocalDateTime.now(), "Test transfer", null, LocalDateTime.now()));

            // Act
            TransactionRecord result = bankingService.transfer(1L, 2L, transferAmount, "Test transfer");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(TransactionType.TRANSFER);
            verify(accountRepository, times(2)).findByIdForUpdate(anyLong());
            verify(accountRepository, times(2)).save(any(Account.class));
            verify(transactionRepository).save(any(TransactionRecord.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException for self-transfer")
        void testTransferToSameAccount() {
            // Arrange
            // Act & Assert
            assertThatThrownBy(() -> bankingService.transfer(1L, 1L, new BigDecimal("100.00"), "Self transfer")).isInstanceOf(BadRequestException.class).hasMessageContaining("Cannot transfer to same account");
            verify(accountRepository, never()).findByIdForUpdate(anyLong());
        }

        @Test
        @DisplayName("Should throw InsufficientFundsException when balance is insufficient")
        void testTransferInsufficientFunds() {
            // Arrange
            when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testAccount));
            when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(testAccount2));

            // Act & Assert
            assertThatThrownBy(() -> bankingService.transfer(1L, 2L, new BigDecimal("2000.00"), "Test")).isInstanceOf(InsufficientFundsException.class);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AccountInactiveException for inactive from account")
        void testTransferFromInactiveAccount() {
            // Arrange
            Account inactiveFromAccount = new Account();
            inactiveFromAccount.setId(1L);
            inactiveFromAccount.setStatus(AccountStatus.SUSPENDED);

            when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(inactiveFromAccount));
            when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(testAccount2));

            // Act & Assert
            assertThatThrownBy(() -> bankingService.transfer(1L, 2L, new BigDecimal("100.00"), "Test")).isInstanceOf(AccountInactiveException.class);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AccountInactiveException for inactive to account")
        void testTransferToInactiveAccount() {
            // Arrange
            Account inactiveToAccount = new Account();
            inactiveToAccount.setId(2L);
            inactiveToAccount.setStatus(AccountStatus.CLOSED);

            when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testAccount));
            when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(inactiveToAccount));

            // Act & Assert
            assertThatThrownBy(() -> bankingService.transfer(1L, 2L, new BigDecimal("100.00"), "Test")).isInstanceOf(AccountInactiveException.class);
        }
    }

    // ============ Transaction Retrieval Tests ============

    @Nested
    @DisplayName("Transaction Retrieval Tests")
    class TransactionRetrievalTests {

        @Test
        @DisplayName("Should get transactions for account")
        void testGetTransactionsForAccount() {
            // Arrange
            List<TransactionRecord> transactions = new ArrayList<>();
            TransactionRecord tx1 = new TransactionRecord();
            tx1.setId(1L);
            tx1.setType(TransactionType.DEPOSIT);
            transactions.add(tx1);

            when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
            when(transactionRepository.findByFromAccountIdOrToAccountIdOrderByTimestampDesc(1L, 1L)).thenReturn(transactions);

            // Act
            List<TransactionRecord> result = bankingService.getTransactionsForAccount(1L);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getType()).isEqualTo(TransactionType.DEPOSIT);
            verify(accountRepository).findById(1L);
            verify(transactionRepository).findByFromAccountIdOrToAccountIdOrderByTimestampDesc(1L, 1L);
        }

        @Test
        @DisplayName("Should throw NotFoundException for non-existent account")
        void testGetTransactionsForNonExistentAccount() {
            // Arrange
            when(accountRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bankingService.getTransactionsForAccount(999L)).isInstanceOf(NotFoundException.class);
            verify(transactionRepository, never()).findByFromAccountIdOrToAccountIdOrderByTimestampDesc(anyLong(), anyLong());
        }
    }
}

