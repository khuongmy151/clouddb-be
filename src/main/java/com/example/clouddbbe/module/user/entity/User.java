package com.example.clouddbbe.module.user.entity;


import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 255, nullable = false, unique = false)
    private String password;

    @Column(name = "fullname", length = 100, nullable = true, unique = false)
    private String fullname;

    @Column(name = "avatar", length = 255, nullable = false, unique = false)
    private String avatar;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column (name = "ban_reason", length = 500)
    private String banReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", updatable = true)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", updatable = false, nullable = true)
    private LocalDateTime deletedAt;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
