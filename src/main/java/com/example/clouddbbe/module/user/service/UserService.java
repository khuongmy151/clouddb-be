package com.example.clouddbbe.module.user.service;

import com.example.clouddbbe.module.user.dto.CreateUserRequest;
import com.example.clouddbbe.module.user.dto.UserResponse;

public interface UserService {
    UserResponse createUser(CreateUserRequest createUserRequest);
}
