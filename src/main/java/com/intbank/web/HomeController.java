package com.intbank.web;

import com.intbank.domain.DomainExceptions.DomainException;
import com.intbank.dto.RegisterRequest;
import com.intbank.service.AuthService;
import com.intbank.service.SimulatorService;
import com.intbank.web.Views.SimulasiItem;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Halaman publik: beranda, login, registrasi, dan bedah OOP. */
@Controller
public class HomeController {

    private final AuthService authService;
    private final SimulatorService simulatorService;

    public HomeController(AuthService authService, SimulatorService simulatorService) {
        this.authService = authService;
        this.simulatorService = simulatorService;
    }

    @GetMapping("/")
    public String landing() {
        return "public/landing";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String locked,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (locked != null) {
            model.addAttribute("errorMessage",
                    "Akun terkunci sementara setelah beberapa kali salah password. Silakan coba lagi nanti.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Kamu sudah keluar dari INT BANK.");
        }
        return "public/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "public/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest request,
                           BindingResult binding,
                           Model model,
                           RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("errorMessage", "Periksa kembali data yang kamu isi.");
            return "public/register";
        }
        try {
            authService.register(request);
        } catch (DomainException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "public/register";
        }
        redirect.addFlashAttribute("successMessage", "Pendaftaran berhasil. Silakan masuk.");
        return "redirect:/login?registered";
    }

    @GetMapping("/bedah-oop")
    public String bedahOop(@RequestParam(required = false) String saldo,
                           @RequestParam(required = false) String nominal,
                           Model model) {
        model.addAttribute("saldo", saldo);
        model.addAttribute("nominal", nominal);
        if (saldo != null && nominal != null && !saldo.isBlank() && !nominal.isBlank()) {
            try {
                List<SimulasiItem> items = simulatorService.simulate(saldo, nominal);
                model.addAttribute("simulasi", items);
            } catch (IllegalArgumentException ex) {
                model.addAttribute("errorMessage", ex.getMessage());
            }
        }
        return "public/bedah-oop";
    }
}
