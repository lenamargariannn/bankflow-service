package com.bankflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Login credentials")
public class AuthRequest {

    @Schema(description = "Username", example = "john_doe")
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Schema(description = "Password", example = "SecurePass123!")
    @NotBlank(message = "Password cannot be blank")
    private String password;
}

