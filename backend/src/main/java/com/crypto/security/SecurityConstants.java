package com.crypto.security;

public class SecurityConstants {
    
    private SecurityConstants() {
        // Private constructor to prevent instantiation
    }
    
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String SIGN_UP_URL = "/api/auth/register";
    public static final String LOGIN_URL = "/api/auth/login";
    public static final String REFRESH_URL = "/api/auth/refresh";
    
    public static final String[] PUBLIC_URLS = {
        "/api/auth/**",
        "/api/public/**",
        "/actuator/health",
        "/actuator/info",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/ws/**"
    };
    
    public static final String[] SWAGGER_WHITELIST = {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };
}