package com.intbank.web;

import com.intbank.domain.*;
import com.intbank.repository.AccountRepository;
import com.intbank.repository.CustomerRepository;
import com.intbank.repository.TransferRepository;
import com.intbank.repository.UserRepository;
import com.intbank.util.FormatUtil;
import com.intbank.web.Views.*;
import org.springframework.stereotype.Component;

import java.util.List;

/** Mengubah entity domain menjadi model tampilan (read-only). */
@Component
public class ViewMapper {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final TransferRepository transferRepository;

    public ViewMapper(AccountRepository accountRepository,
                      UserRepository userRepository,
                      CustomerRepository customerRepository,
                      TransferRepository transferRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.transferRepository = transferRepository;
    }

    public AccountView toAccountView(Account account) {
        Money minimum = switch (account.typeName()) {
            case "Tabungan" -> BankingRules.MINIMUM_SAVINGS_BALANCE;
            default -> Money.ZERO;
        };
        Money fee = switch (account.typeName()) {
            case "Giro" -> BankingRules.CURRENT_ACCOUNT_FEE;
            default -> Money.ZERO;
        };
        String interestRule = switch (account.typeName()) {
            case "Tabungan" -> "3% per tahun (proyeksi)";
            case "Deposito" -> "5% per tahun (proyeksi)";
            default -> "Tidak ada bunga";
        };
        String maturity = account instanceof DepositAccount deposit
                ? FormatUtil.date(deposit.maturityDate())
                : null;

        return new AccountView(
                account.accountNumber(),
                FormatUtil.mask(account.accountNumber()),
                account.typeName(),
                account.status().label(),
                account.status().name(),
                FormatUtil.rupiah(account.balance()),
                FormatUtil.rupiah(minimum),
                FormatUtil.rupiah(fee),
                interestRule,
                maturity,
                FormatUtil.rupiah(account.interestProjection(12)));
    }

    public TransactionView toTransactionView(Transaction transaction) {
        Account account = accountRepository.findById(transaction.getAccountId()).orElse(null);
        String accountNumber = account == null ? "-" : account.accountNumber();
        String destination = "-";
        if (transaction.getType() == TransactionType.TRANSFER_OUT) {
            destination = transferRepository.findAll().stream()
                    .filter(t -> t.getTransactionOutId().equals(transaction.getId()))
                    .findFirst()
                    .map(t -> accountRepository.findById(t.getDestinationAccountId())
                            .map(a -> FormatUtil.mask(a.accountNumber()))
                            .orElse("-"))
                    .orElse("-");
        } else if (transaction.getType() == TransactionType.TRANSFER_IN) {
            destination = transferRepository.findAll().stream()
                    .filter(t -> t.getTransactionInId().equals(transaction.getId()))
                    .findFirst()
                    .map(t -> accountRepository.findById(t.getSourceAccountId())
                            .map(a -> FormatUtil.mask(a.accountNumber()))
                            .orElse("-"))
                    .orElse("-");
        }
        boolean masuk = transaction.getType() == TransactionType.DEPOSIT
                || transaction.getType() == TransactionType.TRANSFER_IN;
        String direction = masuk ? "IN" : "OUT";
        return new TransactionView(
                transaction.getReferenceNumber(),
                transaction.getType().label(),
                FormatUtil.rupiah(transaction.getAmount()),
                FormatUtil.rupiah(transaction.getFee()),
                FormatUtil.rupiah(transaction.getBalanceAfter()),
                FormatUtil.dateTime(transaction.getCreatedAt()),
                transaction.getStatus().label(),
                direction,
                transaction.getDescription(),
                accountNumber,
                destination,
                transaction.getType().name(),
                transaction.getStatus().name());
    }

    public List<TransactionView> toTransactionViews(List<Transaction> transactions) {
        return transactions.stream().map(this::toTransactionView).toList();
    }

    public CustomerView toCustomerView(Customer customer) {
        User user = userRepository.findById(customer.getUserId()).orElse(null);
        List<Account> accounts = accountRepository.findAllByOwnerCustomerId(customer.getId());
        long total = accounts.stream().mapToLong(a -> a.balance().toBigDecimal().longValue()).sum();
        return new CustomerView(
                customer.getId(),
                customer.getFullName(),
                user == null ? "-" : user.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                user != null && user.isEnabled(),
                accounts.size(),
                FormatUtil.rupiah(Money.of(total)),
                FormatUtil.dateTime(customer.getCreatedAt()));
    }

    public AuditLogView toAuditLogView(AuditLog log) {
        String actor = log.getActorUserId() == null ? "sistem" : userRepository.findById(log.getActorUserId())
                .map(User::getEmail).orElse("sistem");
        return new AuditLogView(actor, log.getAction(), log.getTargetType(), log.getTargetId(),
                log.getReason(), FormatUtil.dateTime(log.getCreatedAt()));
    }

    public TransferView toTransferView(Transfer transfer) {
        Account destination = accountRepository.findById(transfer.getDestinationAccountId()).orElse(null);
        String name = "-";
        if (destination != null) {
            name = customerRepository.findById(destination.ownerCustomerId())
                    .map(Customer::getFullName)
                    .orElse("-");
        }
        return new TransferView(transfer.getTransferReference(),
                FormatUtil.mask(destination == null ? "" : destination.accountNumber()), name);
    }
}
