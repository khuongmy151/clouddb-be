package com.example.clouddbbe.module.activity.service;


import com.example.clouddbbe.core.response.PaginationResponse;
import com.example.clouddbbe.module.activity.dto.ActivityLogResponse;
import com.example.clouddbbe.module.activity.entity.ActionType;
import com.example.clouddbbe.module.activity.entity.ActivityLog;
import com.example.clouddbbe.module.activity.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Async
    @Override
    @Transactional
    public void log(Integer idUser, ActionType actionType, String description) {
        save(idUser, null, null, actionType, description);
    }

    @Async
    @Override
    @Transactional
    public void logTargetUser(Integer idUser, Integer targetUserId,
                              ActionType actionType, String description) {
        save(idUser, targetUserId, null, actionType, description);
    }

    @Async
    @Override
    @Transactional
    public void logTargetDb(Integer idUser, Integer targetDbId,
                            ActionType actionType, String description) {
        save(idUser, null, targetDbId, actionType, description);
    }

    private void save(Integer idUser, Integer targetUser, Integer targetDb,
                      ActionType actionType, String description) {
        try {
            ActivityLog activityLog = ActivityLog.builder()
                    .idUser(idUser)
                    .targetUser(targetUser)
                    .targetDb(targetDb)
                    .actionType(actionType)
                    .description(description)
                    .build();
            activityLogRepository.save(activityLog);
        } catch (Exception e) {
            // Không để lỗi ghi log làm hỏng nghiệp vụ chính
            log.error("Failed to write activity log [{}]: {}", actionType, e.getMessage(), e);
        }
    }

    @Override
    public PaginationResponse<ActivityLogResponse> getActivityLog(
            Integer userId,
            ActionType actionType ,
            Integer targetUserId,
            Pageable pageable
    ){
        Page<ActivityLogResponse> responsePage = activityLogRepository
                .searchLogs(userId,actionType,targetUserId,pageable)
                .map(this::mapToResponse);
        return PaginationResponse.of(responsePage);
    }

    @Override
    public ActivityLogResponse mapToResponse(ActivityLog activityLog) {
        return ActivityLogResponse.builder()
                .id(activityLog.getId())
                .idUser(activityLog.getIdUser())
                .targetUser(activityLog.getTargetUser())
                .targetDb(activityLog.getTargetDb())
                .actionType(activityLog.getActionType())
                .description(activityLog.getDescription())
                .createdAt(activityLog.getCreatedAt())
                .build();
    }

    @Override
    public PaginationResponse<ActivityLogResponse> getMyActivityLogs(
            Integer currentUserId,
            ActionType actionType,
            Integer targetUserId,
            Integer targetDbId,
            Pageable pageable
    ) {
        Page<ActivityLogResponse> page = activityLogRepository
                .searchMyLogs(currentUserId, actionType, targetUserId, targetDbId, pageable)
                .map(this::mapToResponse);

        return PaginationResponse.of(page);
    }
}
