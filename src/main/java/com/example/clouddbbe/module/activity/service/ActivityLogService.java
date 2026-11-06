package com.example.clouddbbe.module.activity.service;


import com.example.clouddbbe.core.response.PaginationResponse;
import com.example.clouddbbe.module.activity.dto.ActivityLogResponse;
import com.example.clouddbbe.module.activity.entity.ActionType;
import com.example.clouddbbe.module.activity.entity.ActivityLog;
import org.springframework.data.domain.Pageable;

public interface ActivityLogService {

    /**
     * Ghi log hành động cơ bản (chỉ có user thực hiện).
     *
     * @param idUser      ID người thực hiện hành động
     * @param actionType  Loại hành động
     * @param description Mô tả chi tiết
     */
    void log(Integer idUser, ActionType actionType, String description);

    /**
     * Ghi log hành động tác động lên một user khác.
     *
     * @param idUser       ID người thực hiện
     * @param targetUserId ID người bị tác động
     * @param actionType   Loại hành động
     * @param description  Mô tả chi tiết
     */
    void logTargetUser(Integer idUser, Integer targetUserId,
                       ActionType actionType, String description);

    /**
     * Ghi log hành động tác động lên một database.
     *
     * @param idUser     ID người thực hiện
     * @param targetDbId ID database bị tác động
     * @param actionType Loại hành động
     * @param description Mô tả chi tiết
     */
    void logTargetDb(Integer idUser, Integer targetDbId,
                     ActionType actionType, String description);

    PaginationResponse<ActivityLogResponse> getActivityLog(
            Integer userId,
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
