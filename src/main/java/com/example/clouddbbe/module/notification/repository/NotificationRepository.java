package com.example.clouddbbe.module.notification.repository;

import com.example.clouddbbe.module.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    Page<Notification> findByIdUserOrderByCreatedAtDesc(Integer idUser, Pageable pageable);

    long countByIdUserAndIsReadFalse(Integer idUser);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND n.idUser = :userId")
    int markAsRead(@Param("id") Integer id, @Param("userId") Integer userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.idUser = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Integer userId);
}
