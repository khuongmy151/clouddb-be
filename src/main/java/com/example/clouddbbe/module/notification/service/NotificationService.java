package com.example.clouddbbe.module.notification.service;

import com.example.clouddbbe.module.notification.dto.NotificationResponse;
import com.example.clouddbbe.module.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {

    /**
     * Gửi thông báo: lưu DB + đẩy realtime qua SSE
     */
    void sendNotification(Integer userId,
                          Integer databaseId,
                          String message,
                          NotificationType type,
                          String link);

    /**
     * Client subscribe để nhận thông báo realtime
     */
    SseEmitter subscribe(Integer userId);

    Page<NotificationResponse> getNotifications(Integer userId, Pageable pageable);

    long countUnread(Integer userId);

    void markAsRead(Integer notificationId, Integer userId);

    void markAllAsRead(Integer userId);
}
