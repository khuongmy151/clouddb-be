package com.example.clouddbbe.module.user.service;

import com.example.clouddbbe.core.exception.BusinessException;
import com.example.clouddbbe.core.response.PaginationResponse;
import com.example.clouddbbe.module.activity.entity.ActionType;
import com.example.clouddbbe.module.activity.service.ActivityLogService;
import com.example.clouddbbe.module.user.dto.*;
import com.example.clouddbbe.module.user.entity.Role;
import com.example.clouddbbe.module.user.entity.Status;
import com.example.clouddbbe.module.user.entity.User;
import com.example.clouddbbe.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    private static final String DICEBEAR_BASE =
            "https://api.dicebear.com/10.x/adventurer-neutral/svg"
                    + "?backgroundColor=ffd9b0,ffa8bf"
                    + "&backgroundColorFill=linear"
                    + "&backgroundColorAngle=135&seed=";

    // ==================== CREATE ====================
    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Tên người dùng đã tồn tại", HttpStatus.CONFLICT);
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email đã tồn tại", HttpStatus.CONFLICT);
        }

        Role role = parseRole(request.role());
        Status status = parseStatus(request.status());

        String avatar = DICEBEAR_BASE + request.username();

        User user = User.builder()
                .username(request.username().trim())
                .password(passwordEncoder.encode(request.password()))
                .fullname(request.fullname())
                .email(request.email())
                .role(role)
                .status(status)
                .avatar(avatar)
                .build();

        User saved = userRepository.save(user);

        activityLogService.logTargetUser(
                getCurrentUserId(),
                saved.getId(),
                ActionType.CREATE_USER,
                "Tạo tài khoản mới: " + saved.getUsername()
        );

        return mapToResponse(userRepository.save(user));


    }

    // ==================== VIEW PROFILE ====================
    @Override
    public UserResponse viewProfile(String username) {
        User user = findUserOrThrow(username);
        return mapToResponse(user);
    }

    // ==================== BAN ====================
    @Override
    public UserResponse banUser(String username, BanUserRequest request) {
        User user = findUserOrThrow(username);

        if (user.getStatus() == Status.BANNED) {
            throw new BusinessException(
                    "Người dùng này đã bị chặn trước đó",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (!StringUtils.hasText(request.banReason())) {
            throw new BusinessException(
                    "Lý do chặn không được để trống",
                    HttpStatus.BAD_REQUEST
            );
        }

        user.setStatus(Status.BANNED);
        user.setBanReason(request.banReason().trim());
        user.setUpdatedAt(LocalDateTime.now());
        user.setTokenVersion(user.getTokenVersion() + 1);


        activityLogService.logTargetUser(
                getCurrentUserId(),
                user.getId(),
                ActionType.BAN_USER,
                "Admin da chan user: " + user.getUsername()
        );


        return mapToResponse(userRepository.save(user));
    }

    // ==================== UNBAN ====================
    @Override
    public UserResponse unbanUser(String username) {
        User user = findUserOrThrow(username);

        if (user.getStatus() == Status.ACTIVE) {
            throw new BusinessException(
                    "Người dùng này hiện đang hoạt động, không cần mở khóa",
                    HttpStatus.BAD_REQUEST
            );
        }

        user.setStatus(Status.ACTIVE);
        user.setBanReason(null);
        user.setUpdatedAt(LocalDateTime.now());
        user.setTokenVersion(user.getTokenVersion() + 1);


        activityLogService.logTargetUser(
                getCurrentUserId(),
                user.getId(),
                ActionType.UNBAN_USER,
                "Admin da bo chan user: " + user.getUsername()
        );

        return mapToResponse(userRepository.save(user));
    }

    // ==================== CHANGE PASSWORD ====================
    @Override
    public UserResponse changePassword(String username, ChangePasswordRequest request) {
        User user = findUserOrThrow(username);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(
                    "Mật khẩu hiện tại không đúng",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(
                    "Mật khẩu xác nhận không khớp",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException(
                    "Mật khẩu mới không được trùng với mật khẩu hiện tại",
                    HttpStatus.BAD_REQUEST
            );
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        user.setTokenVersion(user.getTokenVersion() + 1);

        activityLogService.logTargetUser(
                getCurrentUserId(),
                user.getId(),
                ActionType.CHANGE_PASSWORD,
                user.getUsername() + " Da thay doi mat khau"
        );

        return mapToResponse(userRepository.save(user));
    }

    // ==================== GET USERS ====================
    @Override
    public PaginationResponse<UserResponse> getUsers(
            String keyword, Role role, Status status, Pageable pageable
    ) {
        String searchKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;

        Page<UserResponse> responsePage = userRepository
                .searchUsers(searchKeyword, role, status, pageable)
                .map(this::mapToResponse);

        return PaginationResponse.of(responsePage);
    }

    // ==================== UPDATE ====================
    @Override
    public UserResponse updateUser(String username, UpdateUserRequest request) {
        User user = findUserOrThrow(username);

        if (!StringUtils.hasText(request.fullname())) {
            throw new BusinessException(
                    "Họ tên không được để trống",
                    HttpStatus.BAD_REQUEST
            );
        }

        user.setFullname(request.fullname().trim());
        user.setUpdatedAt(LocalDateTime.now());

        activityLogService.logTargetUser(
                getCurrentUserId(),
                user.getId(),
                ActionType.UPDATE_USER,
                user.getUsername() + " Da cap nhat thong tin"
        );


        return mapToResponse(userRepository.save(user));
    }

    // ==================== DELETE ====================
    @Override
    public void deleteUser(String username, String currentUsername) {
        if (username.equals(currentUsername)) {
            throw new BusinessException(
                    "Bạn không thể xóa chính tài khoản của mình",
                    HttpStatus.BAD_REQUEST
            );
        }

        User user = findUserOrThrow(username);

        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException(
                    "Không được phép xóa tài khoản Admin khác",
                    HttpStatus.FORBIDDEN
            );
        }

        activityLogService.logTargetUser(
                getCurrentUserId(),
                user.getId(),
                ActionType.DELETE_USER,
                "Da xoa user: " + user.getUsername()
        );

        userRepository.delete(user);


    }

    // ==================== HELPERS ====================
    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(
                        "Không tìm thấy người dùng với username: " + username,
                        HttpStatus.NOT_FOUND
                ));
    }

    private Role parseRole(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new BusinessException("Role không được để trống", HttpStatus.BAD_REQUEST);
        }
        try {
            return Role.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Role không hợp lệ", HttpStatus.BAD_REQUEST);
        }
    }

    private Status parseStatus(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new BusinessException("Status không được để trống", HttpStatus.BAD_REQUEST);
        }
        try {
            return Status.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Status không hợp lệ", HttpStatus.BAD_REQUEST);
        }
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .build();
    }


    private Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String username = auth.getName();
        if (!StringUtils.hasText(username) || "anonymousUser".equals(username)) {
            return null;
        }
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);
    }
}