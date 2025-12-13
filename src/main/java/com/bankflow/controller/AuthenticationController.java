package com.bankflow.controller;

import com.bankflow.dto.AuthRequest;
import com.bankflow.dto.AuthResponse;
import com.bankflow.dto.SignupRequest;
import com.bankflow.security.JwtTokenProvider;
import com.bankflow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        log.info("AUDIT: Authentication attempt - Username: {}", authRequest.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            String jwt = tokenProvider.generateToken(authentication);
            log.info("AUDIT: User authenticated successfully - Username: {}", authRequest.getUsername());

            return ResponseEntity.ok(new AuthResponse(jwt, "Bearer", 86400));
        } catch (AuthenticationException ex) {
            log.warn("AUDIT: Authentication failed - Username: {}, Error: {}", authRequest.getUsername(), ex.getMessage());
            return ResponseEntity.status(401).body(new AuthResponse(null, "Bearer", 0));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        log.info("AUDIT: User registration attempt - Username: {}, Email: {}",
                signupRequest.getUsername(), signupRequest.getEmail());

        userService.registerUser(signupRequest);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("username", signupRequest.getUsername());

        log.info("AUDIT: User registration successful - Username: {}", signupRequest.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<AuthResponse> validateToken(@RequestParam String token) {
        if (tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsernameFromToken(token);
            log.debug("AUDIT: Token validation successful - Username: {}", username);
            return ResponseEntity.ok(new AuthResponse(null, "valid", 86400));
        } else {
            log.warn("AUDIT: Token validation failed");
            return ResponseEntity.status(401).body(new AuthResponse(null, "invalid", 0));
        }
    }
}

