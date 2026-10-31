package com.example.clouddbbe.module.user.dto;

import com.example.clouddbbe.module.user.entity.Role;
import com.example.clouddbbe.module.user.entity.Status;
import lombok.Builder;

import java.time.LocalDateTime;
@Builder
public record UserResponse(
        String username,
        int id,
        String fullname,
        String email,
        Role role,
        Status status,
        String avatar,
        LocalDateTime createdAt


) {



}
