package com.example.clouddbbe.module.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "Username or Email is required")
        String usernameOrEmail,
        @NotBlank(message = "Password is required")
        String password
) {
}
