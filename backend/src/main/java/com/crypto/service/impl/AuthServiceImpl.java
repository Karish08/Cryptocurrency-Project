package com.crypto.service.impl;

import com.crypto.dto.request.AuthRequest;
import com.crypto.dto.response.AuthResponse;
import com.crypto.model.ActivityLog;
import com.crypto.model.User;
import com.crypto.repository.ActivityLogRepository;
import com.crypto.repository.UserRepository;
import com.crypto.security.JwtTokenProvider;
import com.crypto.service.AuthService;
import com.crypto.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    
    @Override
    @Transactional
    public AuthResponse authenticateUser(AuthRequest loginRequest, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(loginRequest.getUsername());
        
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // Log activity
        logActivity(user, "LOGIN", "User logged in", request);
        
        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .expiresIn(86400L)
                .build();
    }
    
    @Override
    @Transactional
    public AuthResponse registerUser(AuthRequest registerRequest, HttpServletRequest request) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        
        if (userRepository.existsByEmail(registerRequest.getUsername() + "@example.com")) {
            throw new RuntimeException("Email is already in use!");
        }
        
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getUsername() + "@example.com")
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(new HashSet<>(Set.of(User.Role.ROLE_USER)))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        
        user = userRepository.save(user);
        
        // Generate verification token and send email
        String verificationToken = UUID.randomUUID().toString();
        // Save verification token logic here
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        
        // Log activity
        logActivity(user, "REGISTER", "New user registered", request);
        
        // Auto login after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getUsername(),
                        registerRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(registerRequest.getUsername());
        
        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .expiresIn(86400L)
                .build();
    }
    
    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (tokenProvider.validateRefreshToken(refreshToken)) {
            String username = tokenProvider.getUsernameFromToken(refreshToken);
            String newAccessToken = tokenProvider.generateToken(username);
            String newRefreshToken = tokenProvider.generateRefreshToken(username);
            
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Set<String> roles = user.getRoles().stream()
                    .map(Enum::name)
                    .collect(Collectors.toSet());
            
            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .roles(roles)
                    .expiresIn(86400L)
                    .build();
        }
        
        throw new RuntimeException("Invalid refresh token");
    }
    
    @Override
    public void logout(String token) {
        // Add token to blacklist (implement with Redis)
        log.info("User logged out with token: {}", token);
    }
    
    @Override
    public void verifyEmail(String token) {
        // Implement email verification logic
        log.info("Verifying email with token: {}", token);
    }
    
    private void logActivity(User user, String action, String details, HttpServletRequest request) {
        ActivityLog log = ActivityLog.builder()
                .user(user)
                .action(action)
                .details(details)
                .ipAddress(request.getRemoteAddr())
                .userAgent(request.getHeader("User-Agent"))
                .build();
        activityLogRepository.save(log);
    }
}