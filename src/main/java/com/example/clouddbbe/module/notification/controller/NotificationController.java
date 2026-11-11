package com.example.clouddbbe.module.notification.controller;


import com.example.clouddbbe.core.exception.BusinessException;
import com.example.clouddbbe.module.notification.dto.NotificationResponse;
import com.example.clouddbbe.module.notification.entity.NotificationType;
import com.example.clouddbbe.module.notification.service.NotificationService;
import com.example.clouddbbe.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;   // dùng để map username → id

    // Sửa lại method subscribe trong NotificationController
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Authentication authentication) {
        try {
            Integer userId = getCurrentUserId(authentication);
            return notificationService.subscribe(userId);
        } catch (BusinessException e) {
            // Xử lý riêng cho SSE: Trả về một SseEmitter lỗi thay vì ném Exception ra ngoài
            SseEmitter emitter = new SseEmitter();
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("Lỗi xác thực: " + e.getMessage()));
                emitter.complete();
            } catch (IOException ex) {
                // Bỏ qua
            }
            return emitter;
        }
    }
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Integer userId = getCurrentUserId(authentication);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(notificationService.getNotifications(userId, pageable));
    }

    /**
     * Số thông báo chưa đọc
     * GET /api/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> countUnread(Authentication authentication) {
        Integer userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(notificationService.countUnread(userId));
    }

    /**
     * Đánh dấu 1 thông báo đã đọc
     * PATCH /api/notifications/{id}/read
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Integer id,
            Authentication authentication) {

        Integer userId = getCurrentUserId(authentication);
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Đánh dấu tất cả đã đọc
     * PATCH /api/notifications/read-all
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        Integer userId = getCurrentUserId(authentication);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * API test - chỉ dùng để test realtime, xóa sau khi xong
     */
    @PostMapping("/test-send")
    public ResponseEntity<String> testSend(
            @RequestParam Integer userId,
            @RequestParam(required = false) Integer databaseId,
            @RequestParam String message,
            @RequestParam(defaultValue = "System") NotificationType type,
            @RequestParam(required = false) String link) {

        notificationService.sendNotification(userId, databaseId, message, type, link);
        return ResponseEntity.ok("Notification sent to userId = " + userId);
    }

    // ======================== Helper ========================

    /**
     * JWT của bạn trả về username → map sang userId
     */
    private Integer getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("Chưa đăng nhập", HttpStatus.UNAUTHORIZED);
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(
                        "Không tìm thấy người dùng: " + username,
                        HttpStatus.NOT_FOUND
                ))
                .getId();
    }
}
