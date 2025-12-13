package com.bankflow.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Slf4j
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String ENCRYPTION_PREFIX = "ENC:";

    @Value("${app.encryption.key:BankFlowSecretEncryptionKey1234}")
    private String encryptionKey;

    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        if (plaintext.startsWith(ENCRYPTION_PREFIX)) {
            return plaintext;
        }

        try {
            byte[] decodedKey = deriveKeyFromString(encryptionKey);
            SecretKeySpec key = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getEncoder().encodeToString(encryptedBytes);

            log.debug("AUDIT: Field encrypted successfully");
            return ENCRYPTION_PREFIX + encoded;
        } catch (Exception ex) {
            log.error("AUDIT: Encryption failed: {}", ex.getMessage());
            throw new RuntimeException("Failed to encrypt sensitive data", ex);
        }
    }

    public String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) {
            return encrypted;
        }

        if (!encrypted.startsWith(ENCRYPTION_PREFIX)) {
            return encrypted;
        }

        try {
            String encryptedValue = encrypted.substring(ENCRYPTION_PREFIX.length());
            byte[] decodedKey = deriveKeyFromString(encryptionKey);
            SecretKeySpec key = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decodedBytes = Base64.getDecoder().decode(encryptedValue);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            log.debug("AUDIT: Field decrypted successfully");
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            log.error("AUDIT: Decryption failed: {}", ex.getMessage());
            throw new RuntimeException("Failed to decrypt sensitive data", ex);
        }
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(ENCRYPTION_PREFIX);
    }

    private byte[] deriveKeyFromString(String keyString) {
        byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[16];

        for (int i = 0; i < key.length; i++) {
            key[i] = keyBytes[i % keyBytes.length];
        }

        return key;
    }
}

