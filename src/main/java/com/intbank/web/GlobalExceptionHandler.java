package com.intbank.web;

import com.intbank.domain.DomainExceptions.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/** Mengubah kegagalan bisnis menjadi pesan ramah di halaman tujuan. */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public String domain(DomainException ex, HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:" + fallback(request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String denied(HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("errorMessage", "Anda tidak berhak mengakses halaman ini.");
        return "redirect:/error/403";
    }

    @ExceptionHandler(Exception.class)
    public String unexpected(Exception ex, HttpServletRequest request, RedirectAttributes redirect) {
        log.error("Gagal memproses {}", request.getRequestURI(), ex);
        redirect.addFlashAttribute("errorMessage", "Terjadi gangguan. Silakan coba lagi.");
        return "redirect:" + fallback(request);
    }

    private String fallback(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/admin")) {
            return "/admin/dashboard";
        }
        if (uri.startsWith("/nasabah")) {
            return "/nasabah/dashboard";
        }
        if (uri.startsWith("/register")) {
            return "/register";
        }
        return "/";
    }
}
