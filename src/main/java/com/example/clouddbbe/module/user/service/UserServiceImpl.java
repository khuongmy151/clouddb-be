package com.example.clouddbbe.module.user.service;

import com.example.clouddbbe.module.user.dto.CreateUserRequest;
import com.example.clouddbbe.module.user.dto.UserResponse;
import com.example.clouddbbe.module.user.entity.Role;
import com.example.clouddbbe.module.user.entity.Status;
import com.example.clouddbbe.module.user.entity.User;
import com.example.clouddbbe.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tên người dùng đã tồn tại"
            );
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email đã tồn tại"
            );
        }

        Role role;
        Status status;

        try {
            role = Role.valueOf(request.role().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Role không hợp lệ"
            );
        }

        try {
            status = Status.valueOf(request.status().trim());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status không hợp lệ"
            );
        }

        String dicebear = "https://api.dicebear.com/10.x/adventurer-neutral/svg?backgroundColor=ffd9b0,ffa8bf&backgroundColorFill=linear&backgroundColorAngle=135&seed=";
        String avatar = dicebear + request.username();

        User user = User.builder()
                .username(request.username().trim())
                .password(passwordEncoder.encode(request.password()))
                .fullname(request.fullname())
                .email(request.email())
                .role(role)
                .status(status)
                .avatar(avatar)
                .build();

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getUsername(),
                savedUser.getId(),
                savedUser.getFullname(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getStatus(),
                savedUser.getAvatar(),
                savedUser.getCreatedAt()
        );
    }

    @Override
    public UserResponse viewProfile(String username) {

        User user= userRepository.findByUsername(username)
                .orElseThrow(()-> new RuntimeException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .role(user.getRole())
                .email(user.getEmail())
                .status(user.getStatus())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .build();
    }

}
