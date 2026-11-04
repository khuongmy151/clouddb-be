package com.example.clouddbbe.module.user.service;

import com.example.clouddbbe.core.response.PaginationResponse;
import com.example.clouddbbe.module.user.dto.*;
import com.example.clouddbbe.module.user.entity.Role;
import com.example.clouddbbe.module.user.entity.Status;
import com.example.clouddbbe.module.user.entity.User;
import com.example.clouddbbe.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Override
    public UserResponse banUser(String username, BanUserRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy người dùng với username: " + username
                ));

        // Không cho phép chặn chính admin đang đăng nhập (nếu bạn muốn kiểm tra thêm)
        // if (user.getRole() == Role.ADMIN) { ... }

        if (user.getStatus() == Status.BANNED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Người dùng này đã bị chặn trước đó"
            );
        }

        user.setStatus(Status.BANNED);
        user.setBanReason(request.banReason().trim());
        user.setUpdatedAt(LocalDateTime.now());

        // Tăng tokenVersion để vô hiệu hóa tất cả JWT hiện tại của user này
        user.setTokenVersion(user.getTokenVersion() + 1);

        User bannedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(bannedUser.getId())
                .username(bannedUser.getUsername())
                .fullname(bannedUser.getFullname())
                .email(bannedUser.getEmail())
                .role(bannedUser.getRole())
                .status(bannedUser.getStatus())
                .avatar(bannedUser.getAvatar())
                .createdAt(bannedUser.getCreatedAt())
                .build();
    }




    @Override
    public UserResponse unbanUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy người dùng với username: " + username
                ));

        if (user.getStatus() == Status.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Người dùng này hiện đang hoạt động, không cần mở khóa"
            );
        }

        user.setStatus(Status.ACTIVE);
        user.setBanReason(null);          // Xóa lý do ban
        user.setUpdatedAt(LocalDateTime.now());

        // Tăng tokenVersion để buộc user phải đăng nhập lại
        // (nếu bạn muốn giữ session cũ thì bỏ dòng này)
        user.setTokenVersion(user.getTokenVersion() + 1);

        User unbannedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(unbannedUser.getId())
                .username(unbannedUser.getUsername())
                .fullname(unbannedUser.getFullname())
                .email(unbannedUser.getEmail())
                .role(unbannedUser.getRole())
                .status(unbannedUser.getStatus())
                .avatar(unbannedUser.getAvatar())
                .createdAt(unbannedUser.getCreatedAt())
                .build();
    }

    @Override
    public UserResponse changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy người dùng với username: " + username
                ));

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mật khẩu hiện tại không đúng"
            );
        }

        // Kiểm tra xác nhận mật khẩu
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mật khẩu xác nhận không khớp"
            );
        }

        // Không cho phép dùng lại mật khẩu cũ
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mật khẩu mới không được trùng với mật khẩu hiện tại"
            );
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(LocalDateTime.now());

        // Tăng tokenVersion → vô hiệu hóa tất cả JWT cũ (bắt buộc đăng nhập lại)
        user.setTokenVersion(user.getTokenVersion() + 1);

        User updatedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .fullname(updatedUser.getFullname())
                .email(updatedUser.getEmail())
                .role(updatedUser.getRole())
                .status(updatedUser.getStatus())
                .avatar(updatedUser.getAvatar())
                .createdAt(updatedUser.getCreatedAt())
                .build();
    }

    @Override
    public PaginationResponse<UserResponse> getUsers(
            String keyword,
            Role role,
            Status status,
            Pageable pageable
    ) {
        // Chuẩn hóa keyword
        String searchKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<User> page = userRepository.searchUsers(searchKeyword, role, status, pageable);

        Page<UserResponse> responsePage = page.map(user -> UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .build());

        return PaginationResponse.of(responsePage);
    }

    @Override
    public UserResponse updateUser(String username, UpdateUserRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy người dùng với username: " + username
                ));

        // Cập nhật họ tên
        user.setFullname(request.fullname().trim());
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .fullname(updatedUser.getFullname())
                .email(updatedUser.getEmail())
                .role(updatedUser.getRole())
                .status(updatedUser.getStatus())
                .avatar(updatedUser.getAvatar())
                .createdAt(updatedUser.getCreatedAt())
                .build();
    }
}
