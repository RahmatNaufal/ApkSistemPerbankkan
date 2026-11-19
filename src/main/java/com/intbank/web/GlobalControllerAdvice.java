package com.intbank.web;

import com.intbank.domain.Customer;
import com.intbank.repository.CustomerRepository;
import com.intbank.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ControllerAdvice;

/** Variabel auth global yang dipakai semua template (tanpa dialect sec:). */
@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public GlobalControllerAdvice(UserRepository userRepository, CustomerRepository customerRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    @ModelAttribute
    public void authAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)
                && auth.getName() != null;
        boolean admin = false;
        String name = "";

        if (loggedIn) {
            admin = auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            String email = auth.getName();
            Customer customer = customerRepository.findByUserId(
                    userRepository.findByEmail(email).map(com.intbank.domain.User::getId).orElse(null)
            ).orElse(null);
            name = customer == null ? email : customer.getFullName();
        }

        model.addAttribute("currentUser", loggedIn);
        model.addAttribute("isAdmin", admin);
        model.addAttribute("userName", name);
    }
}
