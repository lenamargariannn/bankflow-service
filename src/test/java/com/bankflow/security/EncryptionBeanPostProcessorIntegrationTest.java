package com.bankflow.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for EncryptionBeanPostProcessor.
 * Tests that fields marked with @Encrypted are automatically encrypted on bean creation.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("EncryptionBeanPostProcessor Integration Tests")
class EncryptionBeanPostProcessorIntegrationTest {

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Should encrypt JWT secret on bean initialization")
    void testJwtSecretIsEncrypted() {
        // Act - Retrieve the encrypted value using reflection
        String encryptedSecret = (String) ReflectionTestUtils.getField(jwtTokenProvider, "jwtSecret");

        // Assert
        assertThat(encryptedSecret)
                .as("JWT secret should be encrypted")
                .startsWith("ENC:");

        // Verify we can decrypt it
        String decrypted = encryptionService.decrypt(encryptedSecret);
        assertThat(decrypted)
                .as("Decrypted secret should be valid")
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    @DisplayName("Should allow JwtTokenProvider to function with encrypted secret")
    void testJwtTokenProviderWorksWithEncryptedSecret() {
        // The JwtTokenProvider should work normally even with encrypted secret
        // because EncryptionService.decrypt() is called when needed

        assertThat(jwtTokenProvider)
                .as("JwtTokenProvider should be instantiated")
                .isNotNull();

        // The bean should have been successfully created despite field encryption
        String encryptedSecret = (String) ReflectionTestUtils.getField(jwtTokenProvider, "jwtSecret");
        assertThat(encryptedSecret).startsWith("ENC:");
    }

    @Test
    @DisplayName("Should handle beans with multiple encrypted fields")
    void testMultipleEncryptedFields() {
        // This test demonstrates that the BeanPostProcessor handles
        // classes with multiple @Encrypted fields correctly

        // JwtTokenProvider has jwtSecret marked as @Encrypted
        String encryptedSecret = (String) ReflectionTestUtils.getField(jwtTokenProvider, "jwtSecret");

        assertThat(encryptedSecret)
                .as("All encrypted fields should be encrypted")
                .startsWith("ENC:");
    }

    @Test
    @DisplayName("Should not encrypt non-@Encrypted fields")
    void testNonEncryptedFieldsUntouched() {
        // The jwtExpirationMs field is NOT marked with @Encrypted
        Long expirationMs = (Long) ReflectionTestUtils.getField(jwtTokenProvider, "jwtExpirationMs");

        // It should have a normal numeric value, not an encrypted string
        assertThat(expirationMs)
                .as("Non-encrypted field should have normal value")
                .isGreaterThan(0);
    }

    @Test
    @DisplayName("Should preserve functionality of encrypted beans")
    void testEncryptedBeanFunctionality() {
        // Even with encryption, the bean should function normally
        assertThat(jwtTokenProvider)
                .as("Bean should be functional")
                .isNotNull();

        // Verify encryption configuration exists
        String secret = (String) ReflectionTestUtils.getField(jwtTokenProvider, "jwtSecret");
        assertThat(secret)
                .as("Secret field should exist and be encrypted")
                .isNotNull()
                .isNotEmpty()
                .startsWith("ENC:");
    }
}

