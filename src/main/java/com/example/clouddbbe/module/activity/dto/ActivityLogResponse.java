package com.example.clouddbbe.module.activity.dto;

import com.example.clouddbbe.module.activity.entity.ActionType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ActivityLogResponse(
        Integer id,
        Integer idUser,
        Integer targetUser,
        Integer targetDb,
        ActionType actionType,
        String description,
        LocalDateTime createdAt
) {
}
