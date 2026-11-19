package com.intbank.service;

import com.intbank.domain.Account;
import com.intbank.domain.Customer;
import com.intbank.domain.DomainExceptions.DomainException;
import com.intbank.domain.Money;
import com.intbank.domain.SavingsAccount;
import com.intbank.domain.Transaction;
import com.intbank.domain.TransactionType;
import com.intbank.domain.User;
import com.intbank.domain.UserRole;
import com.intbank.dto.RegisterRequest;
import com.intbank.repository.AccountRepository;
import com.intbank.repository.CustomerRepository;
import com.intbank.repository.TransactionRepository;
import com.intbank.repository.UserRepository;
import com.intbank.util.FormatUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/** Pendaftaran nasabah baru beserta rekening tabungan awal. */
@Service
public class AuthService {

    /** Saldo awal simulasi yang diberikan saat pendaftaran. */
    public static final Money SALDO_AWAL = Money.of(500_000);

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository,
                       CustomerRepository customerRepository,
                       AccountRepository accountRepository,
                       TransactionRepository transactionRepository,
                       AccountNumberGenerator accountNumberGenerator,
                       PasswordEncoder passwordEncoder,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new DomainException("Email tersebut sudah terdaftar.");
        }

        User user = userRepository.save(
                new User(email, passwordEncoder.encode(request.getPassword()), UserRole.CUSTOMER));
        Customer customer = customerRepository.save(new Customer(
                user.getId(), request.getFullName().trim(), request.getPhone().trim(), null));

        Account account = accountRepository.save(
                new SavingsAccount(accountNumberGenerator.generate(), customer.getId(), SALDO_AWAL));
        transactionRepository.save(new Transaction(
                FormatUtil.reference(), account.id(), TransactionType.OPEN_ACCOUNT,
                SALDO_AWAL, Money.ZERO, account.balance(), "Saldo awal simulasi", user.getId()));

        auditService.record("REGISTER", "USER", String.valueOf(user.getId()),
                "Pendaftaran nasabah " + email);
    }
}
