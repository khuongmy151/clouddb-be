package com.example.clouddbbe.module.user.dto;


import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Fullname is required")
        String fullname,
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Password is required")
        String password,
        @NotBlank(message = "Role is required")
        String role,
        @NotBlank(message = "Status is required")
        String status,

        String avatar
) {
}
