package com.intbank.service;

import com.intbank.domain.Account;
import com.intbank.domain.AuditLog;
import com.intbank.domain.Customer;
import com.intbank.domain.DomainExceptions.DomainException;
import com.intbank.domain.Money;
import com.intbank.repository.AccountRepository;
import com.intbank.repository.AuditLogRepository;
import com.intbank.repository.CustomerRepository;
import com.intbank.repository.TransactionRepository;
import com.intbank.repository.UserRepository;
import com.intbank.util.FormatUtil;
import com.intbank.web.ViewMapper;
import com.intbank.web.Views.AccountView;
import com.intbank.web.Views.AuditLogView;
import com.intbank.web.Views.CustomerView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/** Fitur admin: statistik, monitoring nasabah/transaksi/audit, dan perubahan status. */
@Service
@Transactional(readOnly = true)
public class AdminService {

    private static final int PAGE_SIZE = 10;

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;
    private final AccountService accountService;
    private final AuditService auditService;
    private final ViewMapper viewMapper;

    public AdminService(CustomerRepository customerRepository,
                        UserRepository userRepository,
                        AccountRepository accountRepository,
                        TransactionRepository transactionRepository,
                        AuditLogRepository auditLogRepository,
                        AccountService accountService,
                        AuditService auditService,
                        ViewMapper viewMapper) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.auditLogRepository = auditLogRepository;
        this.accountService = accountService;
        this.auditService = auditService;
        this.viewMapper = viewMapper;
    }

    public Map<String, String> stats() {
        Money total = Money.of(accountRepository.sumAllBalances() == null
                ? java.math.BigDecimal.ZERO : accountRepository.sumAllBalances());
        return Map.of(
                "totalCustomers", String.valueOf(customerRepository.count()),
                "totalAccounts", String.valueOf(accountRepository.count()),
                "totalTransactions", String.valueOf(transactionRepository.count()),
                "totalBalanceFormatted", FormatUtil.rupiah(total));
    }

    public Page<CustomerView> customers(String keyword, int page) {
        Pageable pageable = pageable(page);
        if (keyword == null || keyword.isBlank()) {
            return customerRepository.findAll(pageable).map(viewMapper::toCustomerView);
        }
        List<Customer> found = customerRepository.searchByNameOrEmail(keyword.trim());
        List<CustomerView> views = found.stream().map(viewMapper::toCustomerView).toList();
        return new org.springframework.data.domain.PageImpl<>(views, pageable, views.size());
    }

    public CustomerView customer(Long customerId) {
        return viewMapper.toCustomerView(findCustomer(customerId));
    }

    public List<AccountView> accountsOf(Long customerId) {
        return accountRepository.findAllByOwnerCustomerId(customerId).stream()
                .map(viewMapper::toAccountView).toList();
    }

    public List<AuditLogView> auditForCustomer(Long customerId) {
        return auditLogRepository
                .findByTargetTypeAndTargetIdOrderByCreatedAtDesc("CUSTOMER", String.valueOf(customerId))
                .stream().map(viewMapper::toAuditLogView).toList();
    }

    public Page<AuditLogView> auditLogs(String actor, String action, int page) {
        return auditLogRepository.search(blankToNull(action), blankToNull(actor), null, pageable(page))
                .map(viewMapper::toAuditLogView);
    }

    public List<String> statusOptions() {
        return List.of("ACTIVE", "INACTIVE", "BLOCKED", "CLOSED");
    }

    @Transactional
    public void changeStatus(Long customerId, String status, String reason) {
        Customer customer = findCustomer(customerId);
        List<Account> accounts = accountRepository.findAllByOwnerCustomerId(customerId);
        for (Account account : accounts) {
            accountService.applyStatus(account, status);
        }
        auditService.record("STATUS_CHANGE", "CUSTOMER", String.valueOf(customer.getId()),
                "Ubah status " + status + " untuk " + customer.getFullName()
                        + " · alasan: " + (reason == null || reason.isBlank() ? "-" : reason.trim()));
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new DomainException("Nasabah tidak ditemukan."));
    }

    private Pageable pageable(int page) {
        return PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
