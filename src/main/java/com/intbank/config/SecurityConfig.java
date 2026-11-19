package com.intbank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** Aturan akses, login form, logout, dan CSRF. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationProvider authenticationProvider;
    private final LoginSuccessHandler successHandler;
    private final LoginFailureHandler failureHandler;

    public SecurityConfig(CustomAuthenticationProvider authenticationProvider,
                          LoginSuccessHandler successHandler,
                          LoginFailureHandler failureHandler) {
        this.authenticationProvider = authenticationProvider;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/bedah-oop",
                                "/css/**", "/js/**", "/error", "/error/**", "/h2-console/**")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/nasabah/**").hasRole("CUSTOMER")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((request, response, denied) ->
                                response.sendRedirect(request.getContextPath() + "/error/403"))
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendRedirect(request.getContextPath() + "/login")))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
