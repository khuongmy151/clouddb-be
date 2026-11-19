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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    // Định dạng hiển thị: dd/MM/yyyy HH:mm:ss
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Regex bắt các chuỗi ISO LocalDateTime (VD: 2026-11-19T21:42:19.652727200 hoặc 2026-11-19T21:42:19)
    private static final Pattern ISO_DATE_PATTERN = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?)"
    );

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
            // Xử lý format lại thời gian trong description trước khi lưu
            String formattedDescription = formatDescriptionTime(description);

            ActivityLog activityLog = ActivityLog.builder()
                    .idUser(idUser)
                    .targetUser(targetUser)
                    .targetDb(targetDb)
                    .actionType(actionType)
                    .description(formattedDescription)
                    .build();
            activityLogRepository.save(activityLog);
        } catch (Exception e) {
            log.error("Failed to write activity log [{}]: {}", actionType, e.getMessage(), e);
        }
    }

    /**
     * Tìm các chuỗi ISO DateTime trong description và format lại thành dd/MM/yyyy HH:mm:ss
     */
    private String formatDescriptionTime(String description) {
        if (description == null || description.isEmpty()) {
            return description;
        }

        Matcher matcher = ISO_DATE_PATTERN.matcher(description);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String isoDateStr = matcher.group(1);
            try {
                // Parse chuỗi ISO (hỗ trợ cả có và không có phần thập phân giây)
                LocalDateTime dateTime = LocalDateTime.parse(isoDateStr);
                String formattedDate = dateTime.format(FORMATTER);
                // Thay thế bằng chuỗi đã format (dùng Matcher.quoteReplacement để tránh lỗi ký tự $)
                matcher.appendReplacement(sb, Matcher.quoteReplacement(formattedDate));
            } catch (Exception e) {
                // Nếu parse lỗi, giữ nguyên chuỗi gốc
                matcher.appendReplacement(sb, Matcher.quoteReplacement(isoDateStr));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
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