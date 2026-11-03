package com.example.clouddbbe.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BanUserRequest(
        @NotBlank(message = "Lý do chặn không được để trống")
        @Size(max = 500, message = "Lý do chặn không được vượt quá 500 ký tự")
        String banReason
) {
}
