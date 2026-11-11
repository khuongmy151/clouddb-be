package com.example.clouddbbe.module.notification.service;

import com.example.clouddbbe.core.exception.BusinessException;
import com.example.clouddbbe.module.notification.dto.NotificationResponse;
import com.example.clouddbbe.module.notification.entity.Notification;
import com.example.clouddbbe.module.notification.entity.NotificationType;
import com.example.clouddbbe.module.notification.repository.NotificationRepository;
import com.example.clouddbbe.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    // ======================== GỬI THÔNG BÁO ========================

    @Override
    @Transactional
    public void sendNotification(Integer userId,
                                 Integer databaseId,
                                 String message,
                                 NotificationType type,
                                 String link) {

        if (userId == null) {
            throw new BusinessException("userId không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (message == null || message.isBlank()) {
            throw new BusinessException("Nội dung thông báo không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (type == null) {
            throw new BusinessException("Loại thông báo không được để trống", HttpStatus.BAD_REQUEST);
        }

        Notification notification = Notification.builder()
                .idUser(userId)
                .idDB(databaseId)
                .content(message.trim())
                .isRead(false)
                .link(link)
                .typeNotification(type)
                .build();

        notification = notificationRepository.save(notification);

        // Đẩy realtime
        sendToClient(userId, toResponse(notification));
    }

    // ======================== SSE ========================

    @Override
    public SseEmitter subscribe(Integer userId) {
        if (userId == null) {
            throw new BusinessException("userId không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        // Đóng connection cũ
        SseEmitter old = emitters.get(userId);
        if (old != null) {
            old.complete();
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            log.info("SSE completed - userId={}", userId);
            emitters.remove(userId);
        });

        emitter.onTimeout(() -> {
            log.info("SSE timeout - userId={}", userId);
            emitters.remove(userId);
        });

        emitter.onError(e -> {
            log.warn("SSE error - userId={}, message={}", userId, e.getMessage());
            emitters.remove(userId);
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE connected successfully"));
        } catch (IOException e) {
            log.error("Failed to send connected event to userId={}", userId, e);
            emitters.remove(userId);
            throw new BusinessException("Không thể thiết lập kết nối SSE", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return emitter;
    }

    private void sendToClient(Integer userId, NotificationResponse data) {
        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) {
            log.debug("No active SSE connection for userId={}", userId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(data));
        } catch (IOException e) {
            log.warn("Failed to push notification to userId={}, removing emitter", userId);
            emitters.remove(userId);
            emitter.completeWithError(e);
        }
    }

    // ======================== QUERY ========================

    @Override
    public Page<NotificationResponse> getNotifications(Integer userId, Pageable pageable) {
        if (userId == null) {
            throw new BusinessException("userId không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        return notificationRepository
                .findByIdUserOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    public long countUnread(Integer userId) {
        if (userId == null) {
            throw new BusinessException("userId không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        return notificationRepository.countByIdUserAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Integer notificationId, Integer userId) {
        if (notificationId == null || userId == null) {
            throw new BusinessException("Tham số không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        int updated = notificationRepository.markAsRead(notificationId, userId);

        if (updated == 0) {
            throw new BusinessException(
                    "Không tìm thấy thông báo hoặc bạn không có quyền",
                    HttpStatus.NOT_FOUND
            );
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Integer userId) {
        if (userId == null) {
            throw new BusinessException("userId không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        notificationRepository.markAllAsRead(userId);
    }

    // ======================== MAPPER ========================

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .idUser(n.getIdUser())
                .idDB(n.getIdDB())
                .content(n.getContent())
                .isRead(n.getIsRead())
                .link(n.getLink())
                .typeNotification(n.getTypeNotification())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
