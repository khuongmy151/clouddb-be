package com.example.clouddbbe.module.activity.repository;


import com.example.clouddbbe.module.activity.entity.ActionType;
import com.example.clouddbbe.module.activity.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {

    // Admin search - hỗ trợ username + fullname
    @Query("""
        SELECT a FROM ActivityLog a
        JOIN User u ON a.idUser = u.id
        LEFT JOIN User tu ON a.targetUser = tu.id
        WHERE (:username IS NULL OR :username = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))
          AND (:name IS NULL OR :name = '' OR LOWER(u.fullname) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:actionType IS NULL OR a.actionType = :actionType)
          AND (:targetUserId IS NULL OR a.targetUser = :targetUserId)
        ORDER BY a.createdAt DESC
        """)
    Page<ActivityLog> searchLogs(
            @Param("username") String username,
            @Param("name") String name,
            @Param("actionType") ActionType actionType,
            @Param("targetUserId") Integer targetUserId,
            Pageable pageable
    );

    // User xem log của mình
    @Query("""
        SELECT a FROM ActivityLog a
        WHERE (a.idUser = :currentUserId OR a.targetUser = :currentUserId)
          AND (:actionType IS NULL OR a.actionType = :actionType)
          AND (:targetUserId IS NULL OR a.targetUser = :targetUserId)
          AND (:targetDbId IS NULL OR a.targetDb = :targetDbId)
        ORDER BY a.createdAt DESC
        """)
    Page<ActivityLog> searchMyLogs(
            @Param("currentUserId") Integer currentUserId,
            @Param("actionType") ActionType actionType,
            @Param("targetUserId") Integer targetUserId,
            @Param("targetDbId") Integer targetDbId,
            Pageable pageable
    );
}
