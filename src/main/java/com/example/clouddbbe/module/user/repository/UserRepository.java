package com.example.clouddbbe.module.user.repository;

import com.example.clouddbbe.module.user.entity.Role;
import com.example.clouddbbe.module.user.entity.Status;
import com.example.clouddbbe.module.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {

    boolean existsByEmail(String email);
    boolean existsByUsername( String username);
    Optional<User> findByUsername(String username);
    // Cách 1: Query đơn giản (khuyến nghị nếu chỉ cần vài điều kiện)
    @Query("""
            SELECT u FROM User u
            WHERE (:keyword IS NULL OR :keyword = '' OR
                   LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(u.fullname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:role IS NULL OR u.role = :role)
              AND (:status IS NULL OR u.status = :status)
            """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") Role role,
            @Param("status") Status status,
            Pageable pageable
    );
}
