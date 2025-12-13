package com.bankflow.repository;

import com.bankflow.model.Account;
import com.bankflow.model.Customer;
import com.bankflow.model.TransactionRecord;
import com.bankflow.model.User;
import com.bankflow.model.enums.AccountStatus;
import com.bankflow.model.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Repository Integration Tests")
class RepositoryIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    private Customer testCustomer;
    private Account testAccount;
    private Account testAccount2;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("repo_test_user");
        user.setEmail("repo_test_user@example.com");
        user.setFullName("Repo Test User");
        user.setPassword("password");
        user.setEnabled(true);

        testCustomer = new Customer();
        testCustomer.setUser(user);
        testCustomer = customerRepository.save(testCustomer);

        testAccount = new Account();
        testAccount.setCustomer(testCustomer);
        testAccount.setAccountNumber("0000000000001");
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setVersion(0L);
        testAccount.setCreatedAt(LocalDateTime.now());
        testAccount.setUpdatedAt(LocalDateTime.now());
        testAccount = accountRepository.save(testAccount);

        testAccount2 = new Account();
        testAccount2.setCustomer(testCustomer);
        testAccount2.setAccountNumber("0000000000002");
        testAccount2.setBalance(new BigDecimal("500.00"));
        testAccount2.setStatus(AccountStatus.ACTIVE);
        testAccount2.setVersion(0L);
        testAccount2.setCreatedAt(LocalDateTime.now());
        testAccount2.setUpdatedAt(LocalDateTime.now());
        testAccount2 = accountRepository.save(testAccount2);
    }

    @Nested
    @DisplayName("Customer Repository Tests")
    class CustomerRepositoryTests {
        @Test
        @DisplayName("Should find customer by ID")
        void testFindById() {
            Optional<Customer> found = customerRepository.findById(testCustomer.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getUser().getUsername()).isEqualTo("repo_test_user");
        }

        @Test
        @DisplayName("Should find customer by user email")
        void testFindByUserEmail() {
            Optional<Customer> found = customerRepository.findByUser_Email("repo_test_user@example.com");
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(testCustomer.getId());
        }

        @Test
        @DisplayName("Should return empty for non-existent email")
        void testFindByUserEmailNotFound() {
            Optional<Customer> found = customerRepository.findByUser_Email("notfound@example.com");
            assertThat(found).isNotPresent();
        }
    }

    @Nested
    @DisplayName("Account Repository Tests")
    class AccountRepositoryTests {
        @Test
        @DisplayName("Should find account by ID")
        void testFindById() {
            Optional<Account> found = accountRepository.findById(testAccount.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getAccountNumber()).isEqualTo("0000000000001");
        }

        @Test
        @DisplayName("Should find accounts by customer ID")
        void testFindByCustomerId() {
            List<Account> accounts = accountRepository.findByCustomerId(testCustomer.getId());
            assertThat(accounts).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty for non-existent account")
        void testFindByIdNotFound() {
            Optional<Account> found = accountRepository.findById(99999L);
            assertThat(found).isNotPresent();
        }
    }

    @Nested
    @DisplayName("Transaction Repository Tests")
    class TransactionRepositoryTests {
        @Test
        @DisplayName("Should save and retrieve transaction record")
        void testSaveAndFindTransaction() {
            TransactionRecord record = new TransactionRecord();
            record.setFromAccount(testAccount);
            record.setToAccount(testAccount2);
            record.setType(TransactionType.TRANSFER);
            record.setAmount(new BigDecimal("100.00"));
            record.setDescription("Test transfer");
            record.setPerformedBy("repo_test_user");
            record.setTimestamp(LocalDateTime.now());
            record.setCreatedAt(LocalDateTime.now());

            TransactionRecord saved = transactionRepository.save(record);
            assertThat(saved.getId()).isNotNull();

            Optional<TransactionRecord> found = transactionRepository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getAmount()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("Should find transactions by fromAccountId")
        void testFindByFromAccountId() {
            TransactionRecord record = new TransactionRecord();
            record.setFromAccount(testAccount);
            record.setToAccount(testAccount2);
            record.setType(TransactionType.TRANSFER);
            record.setAmount(new BigDecimal("50.00"));
            record.setDescription("Test transfer 2");
            record.setPerformedBy("repo_test_user");
            record.setTimestamp(LocalDateTime.now());
            record.setCreatedAt(LocalDateTime.now());
            transactionRepository.save(record);

            List<TransactionRecord> records = transactionRepository.findByFromAccountIdOrderByTimestampDesc(testAccount.getId());
            assertThat(records).isNotEmpty();
            assertThat(records.getFirst().getAmount()).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("Should return empty for non-existent transaction")
        void testFindByIdNotFound() {
            Optional<TransactionRecord> found = transactionRepository.findById(99999L);
            assertThat(found).isNotPresent();
        }
    }

    @Nested
    @DisplayName("Referential Integrity Tests")
    class ReferentialIntegrityTests {
        @Test
        @DisplayName("Should maintain referential integrity - accounts exist after finding by customer ID")
        void testAccountsExistForCustomer() {
            List<Account> accounts = accountRepository.findByCustomerId(testCustomer.getId());
            assertThat(accounts).isNotEmpty();
            assertThat(accounts).hasSize(2);
        }

        @Test
        @DisplayName("Should persist transaction with account references")
        void testTransactionWithAccountReferences() {
            TransactionRecord record = new TransactionRecord();
            record.setFromAccount(testAccount);
            record.setToAccount(testAccount2);
            record.setType(TransactionType.TRANSFER);
            record.setAmount(new BigDecimal("25.00"));
            record.setDescription("Referential integrity test");
            record.setPerformedBy("repo_test_user");
            record.setTimestamp(LocalDateTime.now());
            record.setCreatedAt(LocalDateTime.now());
            TransactionRecord saved = transactionRepository.save(record);

            Optional<TransactionRecord> found = transactionRepository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getFromAccount().getId()).isEqualTo(testAccount.getId());
            assertThat(found.get().getToAccount().getId()).isEqualTo(testAccount2.getId());
        }
    }
}
