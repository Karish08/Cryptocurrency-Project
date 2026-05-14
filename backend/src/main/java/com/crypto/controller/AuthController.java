package com.crypto.controller;

import com.crypto.dto.request.AuthRequest;
import com.crypto.dto.response.AuthResponse;
import com.crypto.dto.response.ErrorResponse;
import com.crypto.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")

@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "Authenticate user and generate JWT token")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest loginRequest, 
                                   HttpServletRequest request) {
        try {
            log.info("Login attempt for user: {}", loginRequest.getUsername());
            AuthResponse response = authService.authenticateUser(loginRequest, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.getUsername(), e);
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Authentication failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest registerRequest,
                                      HttpServletRequest request) {
        try {
            log.info("Registration attempt for user: {}", registerRequest.getUsername());
            AuthResponse response = authService.registerUser(registerRequest, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Registration failed for user: {}", registerRequest.getUsername(), e);
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Registration failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        try {
            String token = refreshToken.substring(7);
            AuthResponse response = authService.refreshToken(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Token refresh failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            authService.logout(jwt);
            return ResponseEntity.ok().body(new ErrorResponse("Logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Logout failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/verify")
    @Operation(summary = "Verify email address")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            authService.verifyEmail(token);
            return ResponseEntity.ok().body(new ErrorResponse("Email verified successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Email verification failed: " + e.getMessage()));
        }
    }
}