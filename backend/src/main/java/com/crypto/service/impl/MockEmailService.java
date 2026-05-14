package com.crypto.service.impl;

import com.crypto.model.User;
import com.crypto.service.EmailService;
import org.springframework.stereotype.Service;

@Service
public class MockEmailService implements EmailService {
    @Override
    public void sendWelcomeEmail(User user) {}

    @Override
    public void sendAlertEmail(User user, String message) {}

    @Override
    public void sendVerificationEmail(String email, String token) {}
}
