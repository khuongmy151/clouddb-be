package com.example.clouddbbe.module.auth.service;

import com.example.clouddbbe.module.auth.dto.AuthRequest;
import com.example.clouddbbe.module.auth.dto.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest, HttpServletResponse response);

}
