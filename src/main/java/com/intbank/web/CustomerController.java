package com.intbank.web;

import com.intbank.domain.Account;
import com.intbank.domain.Customer;
import com.intbank.domain.Transaction;
import com.intbank.dto.DepositRequest;
import com.intbank.dto.OpenAccountRequest;
import com.intbank.dto.ProfileRequest;
import com.intbank.dto.TransferRequest;
import com.intbank.dto.WithdrawRequest;
import com.intbank.service.AccountService;
import com.intbank.service.CurrentUserService;
import com.intbank.service.TransactionService;
import com.intbank.service.TransferApplicationService;
import com.intbank.web.Views.AccountView;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

/** Seluruh halaman nasabah. */
@Controller
@RequestMapping("/nasabah")
public class CustomerController {

    private final CurrentUserService currentUserService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final TransferApplicationService transferService;
    private final ViewMapper viewMapper;

    public CustomerController(CurrentUserService currentUserService,
                              AccountService accountService,
                              TransactionService transactionService,
                              TransferApplicationService transferService,
                              ViewMapper viewMapper) {
        this.currentUserService = currentUserService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.transferService = transferService;
        this.viewMapper = viewMapper;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Customer customer = currentUserService.currentCustomer();
        model.addAttribute("customerName", customer.getFullName());
        model.addAttribute("totalBalance", accountService.totalBalanceFormatted());
        model.addAttribute("accounts", accountService.myAccounts());
        model.addAttribute("recentTransactions",
                transactionService.recentTransactions(accountService.myAccountIds(), 5));
        return "customer/dashboard";
    }

    @GetMapping("/rekening/buka")
    public String openForm(Model model) {
        OpenAccountRequest request = new OpenAccountRequest();
        if (!model.containsAttribute("openAccountRequest")) {
            model.addAttribute("openAccountRequest", request);
        }
        model.addAttribute("accounts", accountService.myAccounts());
        model.addAttribute("types", accountService.accountTypes());
        return "customer/accounts/open";
    }

    @PostMapping("/rekening/buka")
    public String open(@Valid @ModelAttribute OpenAccountRequest request,
                       BindingResult binding,
                       Model model,
                       RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            prepareOpenForm(model);
            model.addAttribute("errorMessage", "Periksa kembali pilihan rekening kamu.");
            return "customer/accounts/open";
        }
        accountService.openAccount(request);
        redirect.addFlashAttribute("successMessage", "Rekening baru berhasil dibuka.");
        return "redirect:/nasabah/dashboard";
    }

    @GetMapping("/rekening/{accountNumber}")
    public String detail(@PathVariable String accountNumber, Model model) {
        Account account = accountService.ownedByNumber(accountNumber);
        AccountView view = viewMapper.toAccountView(account);
        model.addAttribute("account", view);
        model.addAttribute("transactions", transactionService.forAccount(account.id()));
        return "customer/accounts/detail";
    }

    @GetMapping("/transaksi/setor")
    public String depositForm(Model model) {
        if (!model.containsAttribute("depositRequest")) {
            model.addAttribute("depositRequest", new DepositRequest());
        }
        model.addAttribute("accounts", accountService.myAccounts());
        return "customer/transactions/deposit";
    }

    @PostMapping("/transaksi/setor")
    public String deposit(@Valid @ModelAttribute DepositRequest request,
                          BindingResult binding,
                          Model model,
                          RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("accounts", accountService.myAccounts());
            model.addAttribute("errorMessage", "Periksa kembali rekening dan nominal.");
            return "customer/transactions/deposit";
        }
        accountService.deposit(request);
        redirect.addFlashAttribute("successMessage", "Setor dana berhasil.");
        return "redirect:/nasabah/dashboard";
    }

    @GetMapping("/transaksi/tarik")
    public String withdrawForm(Model model) {
        if (!model.containsAttribute("withdrawRequest")) {
            model.addAttribute("withdrawRequest", new WithdrawRequest());
        }
        model.addAttribute("accounts", accountService.myAccounts());
        return "customer/transactions/withdraw";
    }

    @PostMapping("/transaksi/tarik")
    public String withdraw(@Valid @ModelAttribute WithdrawRequest request,
                           BindingResult binding,
                           Model model,
                           RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("accounts", accountService.myAccounts());
            model.addAttribute("errorMessage", "Periksa kembali rekening dan nominal.");
            return "customer/transactions/withdraw";
        }
        accountService.withdraw(request);
        redirect.addFlashAttribute("successMessage", "Penarikan dana berhasil.");
        return "redirect:/nasabah/dashboard";
    }

    @GetMapping("/transaksi/transfer")
    public String transferForm(Model model) {
        TransferRequest request = new TransferRequest();
        request.setIdempotencyKey(UUID.randomUUID().toString());
        if (!model.containsAttribute("transferRequest")) {
            model.addAttribute("transferRequest", request);
        }
        model.addAttribute("destMasked", null);
        model.addAttribute("accounts", accountService.myAccounts());
        return "customer/transactions/transfer";
    }

    @PostMapping("/transaksi/transfer")
    public String transfer(@Valid @ModelAttribute TransferRequest request,
                           BindingResult binding,
                           Model model,
                           RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("accounts", accountService.myAccounts());
            model.addAttribute("destMasked", com.intbank.util.FormatUtil.mask(request.getDestinationAccountNumber()));
            model.addAttribute("errorMessage", "Periksa kembali data transfer.");
            return "customer/transactions/transfer";
        }
        String reference = transferService.transfer(request);
        redirect.addFlashAttribute("successMessage", "Transfer berhasil. Nomor referensi " + reference);
        return "redirect:/nasabah/transaksi/bukti/" + reference;
    }

    @GetMapping("/transaksi/riwayat")
    public String history(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String type,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) String fromDate,
                          @RequestParam(required = false) String toDate,
                          Model model) {
        model.addAttribute("page", transactionService.historyForCustomer(
                accountService.myAccountIds(), keyword, type, status, fromDate, toDate, page));
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "customer/transactions/history";
    }

    @GetMapping("/transaksi/bukti/{reference}")
    public String receipt(@PathVariable String reference, Model model) {
        Transaction transaction = transactionService.entityByReference(reference);
        accountService.owned(transaction.getAccountId());
        model.addAttribute("transaction", viewMapper.toTransactionView(transaction));
        model.addAttribute("transfer", transactionService.transferFor(transaction));
        return "customer/transactions/receipt";
    }

    @GetMapping("/profil")
    public String profileForm(Model model) {
        Customer customer = currentUserService.currentCustomer();
        ProfileRequest request = new ProfileRequest();
        request.setFullName(customer.getFullName());
        request.setPhone(customer.getPhone());
        request.setAddress(customer.getAddress());
        if (!model.containsAttribute("profileRequest")) {
            model.addAttribute("profileRequest", request);
        }
        model.addAttribute("email", currentUserService.currentUser().getEmail());
        return "customer/profile";
    }

    @PostMapping("/profil")
    public String updateProfile(@Valid @ModelAttribute ProfileRequest request,
                                BindingResult binding,
                                Model model,
                                RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("email", currentUserService.currentUser().getEmail());
            model.addAttribute("errorMessage", "Periksa kembali data profil.");
            return "customer/profile";
        }
        currentUserService.updateProfile(request.getFullName(), request.getPhone(), request.getAddress());
        redirect.addFlashAttribute("successMessage", "Profil berhasil diperbarui.");
        return "redirect:/nasabah/profil";
    }

    private void prepareOpenForm(Model model) {
        model.addAttribute("accounts", accountService.myAccounts());
        model.addAttribute("types", accountService.accountTypes());
    }
}
