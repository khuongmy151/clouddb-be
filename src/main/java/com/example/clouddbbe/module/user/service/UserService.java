package com.example.clouddbbe.module.user.service;

import com.example.clouddbbe.module.user.dto.BanUserRequest;
import com.example.clouddbbe.module.user.dto.ChangePasswordRequest;
import com.example.clouddbbe.module.user.dto.CreateUserRequest;
import com.example.clouddbbe.module.user.dto.UserResponse;

public interface UserService {
    UserResponse createUser(CreateUserRequest createUserRequest);
    UserResponse viewProfile(String username);

    UserResponse banUser(String username, BanUserRequest request);

    UserResponse unbanUser(String username);

    UserResponse changePassword(String username, ChangePasswordRequest request);

}
