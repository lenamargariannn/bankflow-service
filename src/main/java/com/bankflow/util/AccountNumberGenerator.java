package com.bankflow.util;

import com.bankflow.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public final class AccountNumberGenerator {

    private final AccountRepository accountRepository;

    public synchronized String generateAccountNumber() {
        String accountNumber;
        int maxAttempts = 10;
        int attempt = 0;

        do {
            accountNumber = generateRandomNumericAccountNumber();
            attempt++;

            if (attempt >= maxAttempts) {
                log.error("Failed to generate unique account number after {} attempts", maxAttempts);
                throw new RuntimeException("Unable to generate unique account number. Please try again.");
            }
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());

        log.debug("Generated unique account number: {} after {} attempt(s)", accountNumber, attempt);
        return accountNumber;
    }

    private String generateRandomNumericAccountNumber() {
        long timestamp = System.currentTimeMillis();
        int randomPart = (int) (Math.random() * 100000000);
        String accountNumber = String.format("%d%08d", timestamp, randomPart);

        if (accountNumber.length() > 20) {
            accountNumber = accountNumber.substring(accountNumber.length() - 20);
        } else if (accountNumber.length() < 12) {
            accountNumber = String.format("%012d", Long.parseLong(accountNumber));
        }

        return accountNumber;
    }

}
