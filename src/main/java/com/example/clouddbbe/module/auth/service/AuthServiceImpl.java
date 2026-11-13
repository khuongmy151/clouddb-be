package com.example.clouddbbe.module.auth.service;

import com.example.clouddbbe.core.security.CookieUtil;
import com.example.clouddbbe.core.security.JwtService;
import com.example.clouddbbe.module.activity.entity.ActionType;
import com.example.clouddbbe.module.activity.service.ActivityLogService;
import com.example.clouddbbe.module.auth.dto.AuthRequest;
import com.example.clouddbbe.module.auth.dto.AuthResponse;
import com.example.clouddbbe.module.user.entity.Status;
import com.example.clouddbbe.module.user.entity.User;
import com.example.clouddbbe.module.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CookieUtil cookieUtil;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    public AuthServiceImpl(UserRepository userRepository,
                           JwtService jwtService,
                           CookieUtil cookieUtil,
                           PasswordEncoder passwordEncoder, ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.cookieUtil = cookieUtil;
        this.passwordEncoder = passwordEncoder;
        this.activityLogService = activityLogService;



    }

    @Override
    public void logout(HttpServletResponse response) {
        // Xóa cookie token
        cookieUtil.deleteTokenCookie(response);
    }


    @Override
    public AuthResponse login(AuthRequest request, HttpServletResponse response) {
        // 1. Tìm user
        User user = userRepository.findByUserAndEmail(request.usernameOrEmail())
                .or(() -> userRepository.findByUserAndEmail(request.usernameOrEmail()))
                .orElseThrow(() -> new RuntimeException("Sai tài khoản hoặc mật khẩu"));

        // 2. Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Sai tài khoản hoặc mật khẩu");
        }

        // 3. Kiểm tra trạng thái
        if (user.getStatus() == Status.BANNED) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa!");
        }

        // 4. Tạo JWT
        String token = jwtService.generateToken(user.getUsername(), user.getTokenVersion());

        // 5. Set cookie
        cookieUtil.addTokenCookie(response, token);

        activityLogService.logTargetUser(
                user.getId(),
                user.getId(),
                ActionType.LOGIN,
                user.getUsername() + " đã đăng nhập lúc " + LocalDateTime.now()
        );
        return new AuthResponse(user.getUsername(), user.getRole(), user.getAvatar());
    }
}