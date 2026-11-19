package com.intbank.config;

import com.intbank.domain.User;
import com.intbank.repository.UserRepository;
import com.intbank.service.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;

/** Mencatat kegagalan login dan mengunci akun setelah batas tertentu. */
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final UserRepository userRepository;
    private final BankingProperties properties;
    private final AuditService auditService;

    public LoginFailureHandler(UserRepository userRepository,
                               BankingProperties properties,
                               AuditService auditService) {
        this.userRepository = userRepository;
        this.properties = properties;
        this.auditService = auditService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String email = request.getParameter("username");
        boolean locked = false;
        if (email != null && !email.isBlank()) {
            String normalized = email.trim().toLowerCase(Locale.ROOT);
            User user = userRepository.findByEmail(normalized).orElse(null);
            if (user != null) {
                LocalDateTime now = LocalDateTime.now();
                if (!user.isCurrentlyLocked(now)) {
                    user.recordFailedLogin(properties.login().maxAttempts(),
                            properties.login().lockMinutes(), now);
                    userRepository.save(user);
                }
                locked = user.isCurrentlyLocked(now);
                auditService.record("LOGIN_FAILED", "USER", normalized, exception.getClass().getSimpleName());
            }
        }
        response.sendRedirect(request.getContextPath() + (locked ? "/login?locked" : "/login?error"));
    }
}
