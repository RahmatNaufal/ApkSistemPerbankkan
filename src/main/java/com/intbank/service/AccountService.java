package com.intbank.service;

import com.intbank.domain.Account;
import com.intbank.domain.AccountStatus;
import com.intbank.domain.CurrentAccount;
import com.intbank.domain.DepositAccount;
import com.intbank.domain.DomainExceptions.DomainException;
import com.intbank.domain.Money;
import com.intbank.domain.SavingsAccount;
import com.intbank.domain.Transaction;
import com.intbank.domain.TransactionType;
import com.intbank.dto.DepositRequest;
import com.intbank.dto.OpenAccountRequest;
import com.intbank.dto.WithdrawRequest;
import com.intbank.repository.AccountRepository;
import com.intbank.repository.TransactionRepository;
import com.intbank.util.FormatUtil;
import com.intbank.web.ViewMapper;
import com.intbank.web.Views.AccountView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Operasi rekening nasabah. Aturan bisnis tetap berada di kelas domain. */
@Service
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final CurrentUserService currentUserService;
    private final TransactionLimitService limitService;
    private final AuditService auditService;
    private final ViewMapper viewMapper;

    public AccountService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          AccountNumberGenerator accountNumberGenerator,
                          CurrentUserService currentUserService,
                          TransactionLimitService limitService,
                          AuditService auditService,
                          ViewMapper viewMapper) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.currentUserService = currentUserService;
        this.limitService = limitService;
        this.auditService = auditService;
        this.viewMapper = viewMapper;
    }

    public List<AccountView> myAccounts() {
        return accountRepository.findAllByOwnerCustomerId(currentUserService.currentCustomerId())
                .stream().map(viewMapper::toAccountView).toList();
    }

    public String totalBalanceFormatted() {
        Money total = Money.ZERO;
        for (Account a : accountRepository.findAllByOwnerCustomerId(currentUserService.currentCustomerId())) {
            total = total.plus(a.balance());
        }
        return FormatUtil.rupiah(total);
    }

    public List<Long> myAccountIds() {
        return accountRepository.findAllByOwnerCustomerId(currentUserService.currentCustomerId())
                .stream().map(Account::id).toList();
    }

    public AccountView accountDetail(Long accountId) {
        return viewMapper.toAccountView(owned(accountId));
    }

    /** Dipakai controller transaksi untuk memastikan rekening milik nasabah login. */
    public Account owned(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new DomainException("Rekening tidak ditemukan."));
        requireOwnership(account);
        return account;
    }

    public Account ownedByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new DomainException("Nomor rekening tidak ditemukan."));
        requireOwnership(account);
        return account;
    }

    public List<Map<String, String>> accountTypes() {
        return List.of(
                Map.of("value", "SAVINGS", "label", "Tabungan",
                        "description", "Saldo minimum Rp50.000, bunga proyeksi 3% per tahun."),
                Map.of("value", "CURRENT", "label", "Giro",
                        "description", "Tanpa saldo minimum, biaya Rp2.500 per transaksi keluar."),
                Map.of("value", "DEPOSIT", "label", "Deposito",
                        "description", "Tenor 3/6/12 bulan, pencairan penuh setelah jatuh tempo."));
    }

    @Transactional
    public Long openAccount(OpenAccountRequest request) {
        Long customerId = currentUserService.currentCustomerId();
        Long userId = currentUserService.currentUserId();
        Money amount;
        try {
            amount = FormatUtil.parseAmount(request.getAmount());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Nominal dana awal wajib diisi dengan benar.");
        }
        limitService.checkAmount(amount);

        if (request.getSourceAccountNumber() == null || request.getSourceAccountNumber().isBlank()) {
            throw new DomainException("Pilih rekening sumber dana awal.");
        }

        Account source = accountRepository.findByAccountNumberForUpdate(request.getSourceAccountNumber().trim())
                .orElseThrow(() -> new DomainException("Rekening sumber tidak ditemukan."));
        requireOwnership(source);

        String newNumber = accountNumberGenerator.generate();
        Account created = switch (request.getType() == null ? "" : request.getType().trim().toUpperCase()) {
            case "SAVINGS" -> new SavingsAccount(newNumber, customerId, amount);
            case "CURRENT" -> new CurrentAccount(newNumber, customerId, amount);
            case "DEPOSIT" -> {
                Integer tenor = request.getTenorMonths();
                if (tenor == null || !List.of(3, 6, 12).contains(tenor)) {
                    throw new DomainException("Pilih tenor 3, 6, atau 12 bulan.");
                }
                yield new DepositAccount(newNumber, customerId, amount, tenor, LocalDate.now());
            }
            default -> throw new DomainException("Jenis rekening tidak dikenal.");
        };

        Money fee = source.withdraw(amount, LocalDate.now());
        Account saved = accountRepository.save(created);
        transactionRepository.save(new Transaction(FormatUtil.reference(), source.id(),
                TransactionType.WITHDRAWAL, amount, fee, source.balance(),
                "Dana awal rekening " + newNumber, userId));
        transactionRepository.save(new Transaction(FormatUtil.reference(), saved.id(),
                TransactionType.OPEN_ACCOUNT, amount, Money.ZERO, saved.balance(),
                "Pembukaan rekening " + saved.typeName(), userId));
        auditService.record("OPEN_ACCOUNT", "ACCOUNT", newNumber, "Pembukaan rekening " + saved.typeName());
        return saved.id();
    }

    @Transactional
    public void deposit(DepositRequest request) {
        Account account = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber().trim())
                .orElseThrow(() -> new DomainException("Rekening tidak ditemukan."));
        requireOwnership(account);
        Money amount = parse(request.getAmount());
        limitService.checkAmount(amount);

        account.credit(amount);
        transactionRepository.save(new Transaction(FormatUtil.reference(), account.id(),
                TransactionType.DEPOSIT, amount, Money.ZERO, account.balance(),
                "Setor dana", currentUserService.currentUserId()));
        auditService.record("DEPOSIT", "ACCOUNT", account.accountNumber(), "Setor dana "
                + FormatUtil.rupiah(amount));
    }

    @Transactional
    public void withdraw(WithdrawRequest request) {
        Account account = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber().trim())
                .orElseThrow(() -> new DomainException("Rekening tidak ditemukan."));
        requireOwnership(account);
        Money amount = parse(request.getAmount());
        limitService.checkAmount(amount);
        limitService.checkDailyWithdrawal(account.id(), amount);

        Money fee = account.withdraw(amount, LocalDate.now());
        transactionRepository.save(new Transaction(FormatUtil.reference(), account.id(),
                TransactionType.WITHDRAWAL, amount, fee, account.balance(),
                "Tarik dana", currentUserService.currentUserId()));
        auditService.record("WITHDRAWAL", "ACCOUNT", account.accountNumber(), "Tarik dana "
                + FormatUtil.rupiah(amount));
    }

    private Money parse(String input) {
        try {
            return FormatUtil.parseAmount(input);
        } catch (IllegalArgumentException e) {
            throw new DomainException("Nominal harus berupa angka bulat yang lebih dari nol.");
        }
    }

    private void requireOwnership(Account account) {
        if (!account.ownerCustomerId().equals(currentUserService.currentCustomerId())) {
            throw new com.intbank.domain.DomainExceptions.AccountAccessDeniedException();
        }
    }

    /** Dipakai admin: tanpa pengecekan kepemilikan nasabah login. */
    public void applyStatus(Account account, String statusName) {
        AccountStatus status;
        try {
            status = AccountStatus.valueOf(statusName.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Status rekening tidak dikenal.");
        }
        switch (status) {
            case ACTIVE -> account.activate();
            case INACTIVE -> account.deactivate();
            case BLOCKED -> account.block();
            case CLOSED -> account.close();
        }
    }
}
