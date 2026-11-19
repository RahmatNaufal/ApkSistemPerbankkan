package com.intbank.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Menggantikan halaman error default Spring Boot dengan halaman INT BANK. */
@Controller
public class AppErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handle(HttpServletRequest request) {
        Object raw = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = raw instanceof Integer value ? value : 500;
        return switch (status) {
            case 403 -> "error/403";
            case 404 -> "error/404";
            default -> "error/500";
        };
    }

    @GetMapping("/error/403")
    public String forbidden() {
        return "error/403";
    }

    @GetMapping("/error/404")
    public String notFound() {
        return "error/404";
    }

    @GetMapping("/error/500")
    public String serverError() {
        return "error/500";
    }
}
