package com.example.clouddbbe.module.user.service;

import com.example.clouddbbe.core.response.PaginationResponse;
import com.example.clouddbbe.module.user.dto.*;
import com.example.clouddbbe.module.user.entity.Role;
import com.example.clouddbbe.module.user.entity.Status;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse createUser(CreateUserRequest createUserRequest);
    UserResponse viewProfile(String username);

    UserResponse banUser(String username, BanUserRequest request);

    UserResponse unbanUser(String username);

    UserResponse changePassword(String username, ChangePasswordRequest request);

    PaginationResponse<UserResponse> getUsers(
            String keyword,
            Role role,
            Status status,
            Pageable pageable
    );

    UserResponse updateUser(String username, UpdateUserRequest request);

}
