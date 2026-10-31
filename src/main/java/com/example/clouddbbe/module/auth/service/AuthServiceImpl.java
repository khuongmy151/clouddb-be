package com.example.clouddbbe.module.auth.service;

import com.example.clouddbbe.core.security.CookieUtil;
import com.example.clouddbbe.core.security.JwtService;
import com.example.clouddbbe.module.auth.dto.AuthRequest;
import com.example.clouddbbe.module.auth.dto.AuthResponse;
import com.example.clouddbbe.module.user.entity.Status;
import com.example.clouddbbe.module.user.entity.User;
import com.example.clouddbbe.module.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CookieUtil cookieUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository,
                           JwtService jwtService,
                           CookieUtil cookieUtil,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.cookieUtil = cookieUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse login(AuthRequest request, HttpServletResponse response) {
        // 1. Tìm user
        User user = userRepository.findByUsername(request.usernameOrEmail())
                .or(() -> userRepository.findByUsername(request.usernameOrEmail()))
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

        return new AuthResponse(user.getUsername(), user.getRole());
    }
}