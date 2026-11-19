package com.intbank.web;

import com.intbank.service.AdminService;
import com.intbank.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Halaman monitoring dan administrasi. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final TransactionService transactionService;

    public AdminController(AdminService adminService, TransactionService transactionService) {
        this.adminService = adminService;
        this.transactionService = transactionService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.stats());
        model.addAttribute("recentTransactions", transactionService.latest(8));
        return "admin/dashboard";
    }

    @GetMapping("/nasabah")
    public String customers(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String keyword,
                            Model model) {
        model.addAttribute("page", adminService.customers(keyword, page));
        model.addAttribute("keyword", keyword);
        return "admin/customers";
    }

    @GetMapping("/nasabah/{id}")
    public String customerDetail(@PathVariable Long id, Model model) {
        model.addAttribute("customer", adminService.customer(id));
        model.addAttribute("accounts", adminService.accountsOf(id));
        model.addAttribute("statusOptions", adminService.statusOptions());
        model.addAttribute("auditLogs", adminService.auditForCustomer(id));
        return "admin/customer-detail";
    }

    @PostMapping("/nasabah/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam String status,
                               @RequestParam(required = false) String reason,
                               RedirectAttributes redirect) {
        adminService.changeStatus(id, status, reason);
        redirect.addFlashAttribute("successMessage", "Status nasabah berhasil diperbarui.");
        return "redirect:/admin/nasabah/" + id;
    }

    @GetMapping("/transaksi")
    public String transactions(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String fromDate,
                               @RequestParam(required = false) String toDate,
                               Model model) {
        model.addAttribute("page", transactionService.historyForAdmin(
                keyword, type, status, fromDate, toDate, page));
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "admin/transactions";
    }

    @GetMapping("/audit")
    public String audit(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(required = false) String actor,
                        @RequestParam(required = false) String action,
                        Model model) {
        model.addAttribute("page", adminService.auditLogs(actor, action, page));
        model.addAttribute("actor", actor);
        model.addAttribute("action", action);
        return "admin/audit";
    }
}
