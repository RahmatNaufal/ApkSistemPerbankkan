package com.intbank.config;

import com.intbank.domain.User;
import com.intbank.repository.UserRepository;
import com.intbank.service.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/** Reset hitungan gagal login dan arahkan ke dashboard sesuai role. */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuditService auditService;

    public LoginSuccessHandler(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        userRepository.findByEmail(authentication.getName()).ifPresent(user -> {
            user.recordSuccessfulLogin(LocalDateTime.now());
            userRepository.save(user);
        });
        auditService.record("LOGIN_SUCCESS", "USER", authentication.getName(), "Login berhasil");

        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        response.sendRedirect(request.getContextPath() + (admin ? "/admin/dashboard" : "/nasabah/dashboard"));
    }
}
