package com.bankflow.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DTO Validation Tests")
class DTOValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("CreateCustomerRequest Validation")
    class UpdateCustomerRequestValidationTests {

        @Test
        @DisplayName("Should validate a valid CreateCustomerRequest")
        void testValidCreateCustomerRequest() {
            // Arrange
            UpdateCustomerRequest request = UpdateCustomerRequest.builder().fullName("John Doe").email("john@example.com").phoneNumber("+1234567890").build();

            // Act
            Set<ConstraintViolation<UpdateCustomerRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should reject CreateCustomerRequest with blank fullName")
        void testBlankFullName() {
            // Arrange
            UpdateCustomerRequest request = UpdateCustomerRequest.builder().fullName("").email("john@example.com").phoneNumber("+27364567890").build();

            // Act
            Set<ConstraintViolation<UpdateCustomerRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fullName"));
        }

        @Test
        @DisplayName("Should reject CreateCustomerRequest with null fullName")
        void testNullFullName() {
            // Arrange
            UpdateCustomerRequest request = UpdateCustomerRequest.builder().fullName("").email("john@example.com").build();

            // Act
            Set<ConstraintViolation<UpdateCustomerRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fullName"));
        }

        @Test
        @DisplayName("Should reject CreateCustomerRequest with invalid email")
        void testInvalidEmail() {
            // Arrange
            UpdateCustomerRequest request = UpdateCustomerRequest.builder().fullName("John Doe").email("not-an-email").build();

            // Act
            Set<ConstraintViolation<UpdateCustomerRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }

        @Test
        @DisplayName("Should reject CreateCustomerRequest with blank email")
        void testBlankEmail() {
            // Arrange
            UpdateCustomerRequest request = UpdateCustomerRequest.builder().fullName("John Doe").email("cwscd").phoneNumber("3293dj").build();

            // Act
            Set<ConstraintViolation<UpdateCustomerRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(2);
        }

        @Test
        @DisplayName("Should accept CreateCustomerRequest with null phoneNumber")
        void testNullPhoneNumber() {
            // Arrange
            UpdateCustomerRequest request = UpdateCustomerRequest.builder().fullName("John Doe").email("john@example.com").phoneNumber("+27364567890").build();

            // Act
            Set<ConstraintViolation<UpdateCustomerRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("CreateAccountRequest Validation")
    class CreateAccountRequestValidationTests {

        @Test
        @DisplayName("Should validate a valid CreateAccountRequest")
        void testValidCreateAccountRequest() {
            // Arrange
            CreateAccountRequest request = CreateAccountRequest.builder().initialDeposit(new BigDecimal("1000.00")).build();

            // Act
            Set<ConstraintViolation<CreateAccountRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should validate CreateAccountRequest with zero initial deposit")
        void testZeroInitialDeposit() {
            // Arrange
            CreateAccountRequest request = CreateAccountRequest.builder().initialDeposit(new BigDecimal("0.00")).build();

            // Act
            Set<ConstraintViolation<CreateAccountRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should reject CreateAccountRequest with negative initial deposit")
        void testNegativeInitialDeposit() {
            CreateAccountRequest request = CreateAccountRequest.builder().initialDeposit(new BigDecimal("-100.00")).build();

            // Act
            Set<ConstraintViolation<CreateAccountRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("initialDeposit"));
        }

        @Test
        @DisplayName("Should accept CreateAccountRequest with null initial deposit")
        void testNullInitialDeposit() {
            // Arrange
            CreateAccountRequest request = CreateAccountRequest.builder().initialDeposit(null).build();

            Set<ConstraintViolation<CreateAccountRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("AmountRequest Validation")
    class AmountRequestValidationTests {

        @Test
        @DisplayName("Should validate a valid AmountRequest")
        void testValidAmountRequest() {
            // Arrange
            AmountRequest request = AmountRequest.builder().amount(new BigDecimal("100.00")).build();

            // Act
            Set<ConstraintViolation<AmountRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should validate AmountRequest with minimum valid amount (0.01)")
        void testMinimumValidAmount() {
            // Arrange
            AmountRequest request = AmountRequest.builder().amount(new BigDecimal("0.01")).build();

            // Act
            Set<ConstraintViolation<AmountRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should reject AmountRequest with zero amount")
        void testZeroAmount() {
            // Arrange
            AmountRequest request = AmountRequest.builder().amount(new BigDecimal("0.00")).build();
            // Act
            Set<ConstraintViolation<AmountRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
        }

        @Test
        @DisplayName("Should reject AmountRequest with negative amount")
        void testNegativeAmount() {
            // Arrange
            AmountRequest request = AmountRequest.builder().amount(new BigDecimal("-50.00")).build();
            // Act
            Set<ConstraintViolation<AmountRequest>> violations = validator.validate(request);
            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
        }

        @Test
        @DisplayName("Should reject AmountRequest with null amount")
        void testNullAmount() {
            // Arrange
            AmountRequest request = AmountRequest.builder().amount(null).build();
            // Act
            Set<ConstraintViolation<AmountRequest>> violations = validator.validate(request);
            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
        }
    }

    @Nested
    @DisplayName("TransferRequest Validation")
    class TransferRequestValidationTests {

        @Test
        @DisplayName("Should validate a valid TransferRequest")
        void testValidTransferRequest() {
            // Arrange
            TransferRequest request = TransferRequest.builder().fromAccountNumber("1734567890123456").toAccountNumber("1734567890123457").amount(new BigDecimal("500.00")).description("Transfer between accounts").build();

            // Act
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should validate TransferRequest with null description")
        void testNullDescription() {
            // Arrange
            TransferRequest request = TransferRequest.builder().fromAccountNumber("1734567890123456").toAccountNumber("1734567890123457").amount(new BigDecimal("500.00")).description(null).build();

            // Act
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should reject TransferRequest with null fromAccountNumber")
        void testNullFromAccountNumber() {
            // Arrange
            TransferRequest request = TransferRequest.builder().fromAccountNumber(null).toAccountNumber("1734567890123457").amount(new BigDecimal("500.00")).build();

            // Act
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fromAccountNumber"));
        }

        @Test
        @DisplayName("Should reject TransferRequest with null toAccountNumber")
        void testNullToAccountNumber() {
            // Arrange
            TransferRequest request = TransferRequest.builder().fromAccountNumber("1734567890123456").toAccountNumber(null).amount(new BigDecimal("500.00")).build();

            // Act
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("toAccountNumber"));
        }

        @Test
        @DisplayName("Should reject TransferRequest with null amount")
        void testNullAmount() {
            // Arrange
            TransferRequest request = TransferRequest.builder().fromAccountNumber("1734567890123456").toAccountNumber("1734567890123457").amount(null).build();

            // Act
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
        }

        @Test
        @DisplayName("Should reject TransferRequest with negative amount")
        void testNegativeAmount() {
            // Arrange
            TransferRequest request = TransferRequest.builder().fromAccountNumber("1734567890123456").toAccountNumber("1734567890123457").amount(new BigDecimal("-100.00")).build();

            // Act
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
        }

        @Test
        @DisplayName("Should reject TransferRequest with zero amount")
        void testZeroAmount() {
            // Arrange
            TransferRequest request = TransferRequest.builder().fromAccountNumber("1734567890123456").toAccountNumber("1734567890123457").amount(new BigDecimal("0.00")).build();

            // Act
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
        }

        @Test
        @DisplayName("Should reject TransferRequest with amount less than minimum (0.01)")
        void testBelowMinimumAmount() {
            // Arrange
            TransferRequest request = TransferRequest.builder().fromAccountNumber("1734567890123456").toAccountNumber("1734567890123457").amount(new BigDecimal("0.001")).build();

            // Act
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
        }

        @Test
        @DisplayName("Should reject TransferRequest with multiple null violations")
        void testMultipleNullViolations() {
            // Arrange
            TransferRequest request = TransferRequest.builder().fromAccountNumber(null).toAccountNumber(null).amount(null).build();

            // Act
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(3);
            assertThat(violations).extracting(v -> v.getPropertyPath().toString()).containsExactlyInAnyOrder("fromAccountNumber", "toAccountNumber", "amount");
        }
    }

    @Nested
    @DisplayName("Response DTOs Instantiation Tests")
    class ResponseDTOInstantiationTests {

        @Test
        @DisplayName("Should create CustomerResponse with all fields")
        void testCreateCustomerResponse() {
            // Act
            CustomerResponse response = CustomerResponse.builder().id(1L).fullName("John Doe").email("john@example.com").phoneNumber("+1234567890").build();

            // Assert
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getFullName()).isEqualTo("John Doe");
            assertThat(response.getEmail()).isEqualTo("john@example.com");
            assertThat(response.getPhoneNumber()).isEqualTo("+1234567890");
        }

        @Test
        @DisplayName("Should create AccountResponse with all fields")
        void testCreateAccountResponse() {
            // Act
            AccountResponse response = AccountResponse.builder().id(1L).accountNumber("ACC001").customerId(1L).balance(new BigDecimal("1000.00")).build();

            // Assert
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getAccountNumber()).isEqualTo("ACC001");
            assertThat(response.getCustomerId()).isEqualTo(1L);
            assertThat(response.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("Should create TransactionResponse with all fields")
        void testCreateTransactionResponse() {
            // Act
            TransactionResponse response = TransactionResponse.builder().id(1L).fromAccountId(1L).toAccountId(2L).amount(new BigDecimal("500.00")).build();

            // Assert
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getFromAccountId()).isEqualTo(1L);
            assertThat(response.getToAccountId()).isEqualTo(2L);
            assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        }
    }
}
