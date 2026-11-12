package com.example.clouddbbe.module.auth.controller;

import com.example.clouddbbe.module.auth.dto.AuthRequest;
import com.example.clouddbbe.module.auth.dto.AuthResponse;
import com.example.clouddbbe.module.auth.service.AuthService;
import com.example.clouddbbe.module.user.entity.User;
import com.example.clouddbbe.module.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;


    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;

        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(authResponse);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok("Đăng xuất thành công");
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(Authentication authentication) {
        // authentication.getName() = username (JwtFilter đã set)
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(new AuthResponse(user.getUsername(), user.getRole(), user.getAvatar()));
    }
}
