package com.example.clouddbbe.module.activity.service;


import com.example.clouddbbe.core.response.PaginationResponse;
import com.example.clouddbbe.module.activity.dto.ActivityLogResponse;
import com.example.clouddbbe.module.activity.entity.ActionType;
import com.example.clouddbbe.module.activity.entity.ActivityLog;
import com.example.clouddbbe.module.activity.repository.ActivityLogRepository;
import com.example.clouddbbe.module.user.entity.User;
import com.example.clouddbbe.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

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
            log.error("Failed to write activity log [{}]: {}", actionType, e.getMessage(), e);
        }
    }

    @Override
    public PaginationResponse<ActivityLogResponse> getActivityLog(
            String username,
            String name,
            ActionType actionType,
            Integer targetUserId,
            Pageable pageable
    ) {
        Page<ActivityLog> page = activityLogRepository.searchLogs(
                username, name, actionType, targetUserId, pageable
        );

        // Lấy tất cả user liên quan trong 1 lần query để tránh N+1
        Set<Integer> userIds = page.getContent().stream()
                .flatMap(log -> java.util.stream.Stream.of(log.getIdUser(), log.getTargetUser()))
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Integer, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Page<ActivityLogResponse> responsePage = page.map(log -> mapToResponse(log, userMap));

        return PaginationResponse.of(responsePage);
    }

    @Override
    public ActivityLogResponse mapToResponse(ActivityLog activityLog) {
        // fallback (ít dùng)
        return mapToResponse(activityLog, Map.of());
    }

    private ActivityLogResponse mapToResponse(ActivityLog activityLog, Map<Integer, User> userMap) {
        User user = userMap.get(activityLog.getIdUser());
        User targetUser = activityLog.getTargetUser() != null
                ? userMap.get(activityLog.getTargetUser())
                : null;

        return ActivityLogResponse.builder()
                .id(activityLog.getId())
                .idUser(activityLog.getIdUser())
                .username(user != null ? user.getUsername() : null)
                .fullname(user != null ? user.getFullname() : null)
                .targetUser(activityLog.getTargetUser())
                .targetUsername(targetUser != null ? targetUser.getUsername() : null)
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
        Page<ActivityLog> page = activityLogRepository.searchMyLogs(
                currentUserId, actionType, targetUserId, targetDbId, pageable
        );

        Set<Integer> userIds = page.getContent().stream()
                .flatMap(log -> java.util.stream.Stream.of(log.getIdUser(), log.getTargetUser()))
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Integer, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Page<ActivityLogResponse> responsePage = page.map(log -> mapToResponse(log, userMap));

        return PaginationResponse.of(responsePage);
    }
}
