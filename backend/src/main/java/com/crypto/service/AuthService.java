package com.crypto.service;

import com.crypto.dto.request.AuthRequest;
import com.crypto.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResponse authenticateUser(AuthRequest loginRequest, HttpServletRequest request);
    AuthResponse registerUser(AuthRequest registerRequest, HttpServletRequest request);
    AuthResponse refreshToken(String refreshToken);
    void logout(String token);
    void verifyEmail(String token);
}
