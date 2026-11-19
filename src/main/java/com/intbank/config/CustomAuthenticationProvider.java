package com.intbank.config;

import com.intbank.domain.User;
import com.intbank.repository.UserRepository;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Menolak login saat akun masih dalam masa kunci. Pengecekan dilakukan sebelum
 * password diverifikasi, sehingga password yang benar pun tidak bisa dipakai
 * selama masa kunci belum berakhir.
 */
@Component
public class CustomAuthenticationProvider extends DaoAuthenticationProvider {

    private final UserRepository userRepository;

    public CustomAuthenticationProvider(CustomUserDetailsService userDetailsService,
                                        PasswordEncoder passwordEncoder,
                                        UserRepository userRepository) {
        super(userDetailsService);
        this.userRepository = userRepository;
        setPasswordEncoder(passwordEncoder);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user != null && user.isCurrentlyLocked(LocalDateTime.now())) {
            throw new LockedException("Akun terkunci sementara. Silakan coba lagi beberapa saat.");
        }
        super.additionalAuthenticationChecks(userDetails, authentication);
    }
}
