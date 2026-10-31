package com.example.clouddbbe.module.auth.dto;

import com.example.clouddbbe.module.user.entity.Role;

public record AuthResponse(
        String username,
        Role role

) {
}
