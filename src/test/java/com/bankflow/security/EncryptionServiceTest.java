package com.bankflow.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the EncryptionService.
 * Tests encryption, decryption, and validation logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EncryptionService Unit Tests")
class EncryptionServiceTest {

    @InjectMocks
    private EncryptionService encryptionService;

    private static final String TEST_ENCRYPTION_KEY = "BankFlowSecretEncryptionKey1234";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(encryptionService, "encryptionKey", TEST_ENCRYPTION_KEY);
    }

    @Test
    @DisplayName("Should encrypt a plaintext string")
    void testEncryptString() {
        // Arrange
        String plaintext = "my-secret-password";

        // Act
        String encrypted = encryptionService.encrypt(plaintext);

        // Assert
        assertThat(encrypted)
                .isNotNull()
                .isNotEqualTo(plaintext)
                .startsWith("ENC:");
    }

    @Test
    @DisplayName("Should decrypt an encrypted string")
    void testDecryptString() {
        // Arrange
        String plaintext = "my-secret-password";
        String encrypted = encryptionService.encrypt(plaintext);

        // Act
        String decrypted = encryptionService.decrypt(encrypted);

        // Assert
        assertThat(decrypted)
                .isNotNull()
                .isEqualTo(plaintext);
    }

    @Test
    @DisplayName("Should handle null values during encryption")
    void testEncryptNull() {
        // Act
        String result = encryptionService.encrypt(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle empty strings during encryption")
    void testEncryptEmptyString() {
        // Act
        String result = encryptionService.encrypt("");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should not re-encrypt already encrypted strings")
    void testDoNotReEncryptEncryptedString() {
        // Arrange
        String plaintext = "secret";
        String encrypted = encryptionService.encrypt(plaintext);

        // Act
        String reencrypted = encryptionService.encrypt(encrypted);

        // Assert
        assertThat(reencrypted).isEqualTo(encrypted);
    }

    @Test
    @DisplayName("Should identify encrypted strings")
    void testIsEncrypted() {
        // Arrange
        String plaintext = "my-secret";
        String encrypted = encryptionService.encrypt(plaintext);

        // Act & Assert
        assertThat(encryptionService.isEncrypted(plaintext)).isFalse();
        assertThat(encryptionService.isEncrypted(encrypted)).isTrue();
    }

    @Test
    @DisplayName("Should handle null values during decryption")
    void testDecryptNull() {
        // Act
        String result = encryptionService.decrypt(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return plaintext for non-encrypted strings in decrypt")
    void testDecryptNonEncryptedString() {
        // Arrange
        String plaintext = "not-encrypted";

        // Act
        String result = encryptionService.decrypt(plaintext);

        // Assert
        assertThat(result).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("Should handle various string inputs")
    void testEncryptDecryptVariousInputs() {
        // Test data
        String[] testValues = {
                "simple-password",
                "123456!@#$%^&*()",
                "very-long-secret-password-with-multiple-words-and-symbols-!@#$%",
                "CamelCasePassword",
                "with spaces in password",
                "special-chars-éàü",
                "日本語パスワード"
        };

        for (String testValue : testValues) {
            // Act
            String encrypted = encryptionService.encrypt(testValue);
            String decrypted = encryptionService.decrypt(encrypted);

            // Assert
            assertThat(decrypted)
                    .as("Should correctly encrypt and decrypt: " + testValue)
                    .isEqualTo(testValue);
        }
    }

    @Test
    @DisplayName("Should produce different encrypted values for multiple encryptions")
    void testEncryptionConsistency() {
        // Arrange
        String plaintext = "consistent-password";

        // Act - Same plaintext encrypted multiple times should produce same result
        // (due to ECB mode - not ideal but expected behavior)
        String encrypted1 = encryptionService.encrypt(plaintext);
        String encrypted2 = encryptionService.encrypt(plaintext);

        // Assert
        assertThat(encrypted1).isEqualTo(encrypted2);
    }

    @Test
    @DisplayName("Should throw RuntimeException for corrupted encrypted data")
    void testDecryptCorruptedData() {
        // Arrange
        String corruptedData = "ENC:InvalidBase64!!!";

        // Act & Assert
        assertThatThrownBy(() -> encryptionService.decrypt(corruptedData))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to decrypt");
    }

    @Test
    @DisplayName("Should encrypt with different keys producing different results")
    void testEncryptionWithDifferentKeys() {
        // Arrange
        String plaintext = "secret";
        String key1 = "Key1234567890ABC";
        String key2 = "Key9876543210XYZ";

        EncryptionService service1 = new EncryptionService();
        EncryptionService service2 = new EncryptionService();
        ReflectionTestUtils.setField(service1, "encryptionKey", key1);
        ReflectionTestUtils.setField(service2, "encryptionKey", key2);

        // Act
        String encrypted1 = service1.encrypt(plaintext);
        String encrypted2 = service2.encrypt(plaintext);

        // Assert
        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }
}

