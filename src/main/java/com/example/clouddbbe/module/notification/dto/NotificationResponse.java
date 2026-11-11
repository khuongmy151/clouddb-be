package com.example.clouddbbe.module.notification.dto;


import com.example.clouddbbe.module.notification.entity.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Integer id;
    private Integer idUser;
    private Integer idDB;
    private String content;
    private Boolean isRead;
    private String link;
    private NotificationType typeNotification;
    private LocalDateTime createdAt;
}
