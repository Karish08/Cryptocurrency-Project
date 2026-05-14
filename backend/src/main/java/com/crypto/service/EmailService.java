package com.crypto.service;

import com.crypto.model.User;

public interface EmailService {
    void sendWelcomeEmail(User user);
    void sendAlertEmail(User user, String message);
    void sendVerificationEmail(String email, String token);
}
