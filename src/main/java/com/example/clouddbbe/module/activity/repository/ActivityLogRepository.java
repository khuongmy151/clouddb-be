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

    @Query("""
        SELECT a FROM ActivityLog a
        WHERE (:userId IS NULL OR a.idUser = :userId)
          AND (:actionType IS NULL OR a.actionType = :actionType)
          AND (:targetUserId IS NULL OR a.targetUser = :targetUserId)
    """)
    Page<ActivityLog> searchLogs(
            @Param("userId") Integer userId,
            @Param("actionType") ActionType actionType,
            @Param("targetUserId") Integer targetUserId,
            Pageable pageable
    );

    @Query("""
        SELECT a FROM ActivityLog a
        WHERE (a.idUser = :currentUserId OR a.targetUser = :currentUserId)
          AND (:actionType IS NULL OR a.actionType = :actionType)
          AND (:targetUserId IS NULL OR a.targetUser = :targetUserId)
          AND (:targetDbId IS NULL OR a.targetDb = :targetDbId)
    """)
    Page<ActivityLog> searchMyLogs(
            @Param("currentUserId") Integer currentUserId,
            @Param("actionType") ActionType actionType,
            @Param("targetUserId") Integer targetUserId,
            @Param("targetDbId") Integer targetDbId,
            Pageable pageable
    );
}
