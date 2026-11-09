package com.example.clouddbbe.module.activity.service;


import com.example.clouddbbe.core.response.PaginationResponse;
import com.example.clouddbbe.module.activity.dto.ActivityLogResponse;
import com.example.clouddbbe.module.activity.entity.ActionType;
import com.example.clouddbbe.module.activity.entity.ActivityLog;
import org.springframework.data.domain.Pageable;

public interface ActivityLogService {

    void log(Integer idUser, ActionType actionType, String description);

    void logTargetUser(Integer idUser, Integer targetUserId,
                       ActionType actionType, String description);

    void logTargetDb(Integer idUser, Integer targetDbId,
                     ActionType actionType, String description);

    // Đã đổi tham số từ userId → username + name
    PaginationResponse<ActivityLogResponse> getActivityLog(
            String username,
            String name,
            ActionType actionType,
            Integer targetUserId,
            Pageable pageable
    );

    ActivityLogResponse mapToResponse(ActivityLog activityLog);

    PaginationResponse<ActivityLogResponse> getMyActivityLogs(
            Integer currentUserId,
            ActionType actionType,
            Integer targetUserId,
            Integer targetDbId,
            Pageable pageable
    );

}
