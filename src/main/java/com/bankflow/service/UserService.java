package com.bankflow.service;

import com.bankflow.dto.SignupRequest;
import com.bankflow.exception.BadRequestException;
import com.bankflow.model.Customer;
import com.bankflow.model.User;
import com.bankflow.repository.CustomerRepository;
import com.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public void registerUser(SignupRequest signupRequest) {
        String username = signupRequest.getUsername();
        String email = signupRequest.getEmail();
        log.info("AUDIT: Registering new user - Username: {}, Email: {}", username, email);
        validateUserUniqueness(username, email);
        Customer customer = createCustomerWithUser(signupRequest);
        Customer savedCustomer = customerRepository.saveAndFlush(customer);
        assignCustomerRole(savedCustomer.getUser().getUsername());
        log.info("AUDIT: User and Customer registered successfully - Username: {}", savedCustomer.getUser().getUsername());
    }

    private void validateUserUniqueness(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            log.warn("AUDIT: Registration failed - Username already exists: {}", username);
            throw new BadRequestException("username", username, "Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            log.warn("AUDIT: Registration failed - Email already exists: {}", email);
            throw new BadRequestException("email", email, "Email already exists");
        }
    }

    private Customer createCustomerWithUser(SignupRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setPhoneNumber(request.getPhoneNumber());

        return customer;
    }

    private void assignCustomerRole(String username) {
        String sql = "INSERT INTO authorities (username, authority, created_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, username, "ROLE_CUSTOMER", LocalDateTime.now());
    }
}

