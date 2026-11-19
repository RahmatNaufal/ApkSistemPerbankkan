package com.intbank.config;

import com.intbank.domain.Account;
import com.intbank.domain.CurrentAccount;
import com.intbank.domain.Customer;
import com.intbank.domain.DepositAccount;
import com.intbank.domain.Money;
import com.intbank.domain.SavingsAccount;
import com.intbank.domain.Transaction;
import com.intbank.domain.TransactionType;
import com.intbank.domain.Transfer;
import com.intbank.domain.TransferService;
import com.intbank.domain.User;
import com.intbank.domain.UserRole;
import com.intbank.repository.AccountRepository;
import com.intbank.repository.CustomerRepository;
import com.intbank.repository.TransactionRepository;
import com.intbank.repository.TransferRepository;
import com.intbank.repository.UserRepository;
import com.intbank.service.AccountNumberGenerator;
import com.intbank.util.FormatUtil;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Data contoh untuk demo. Idempoten: hanya dibuat saat startup pertama. */
@Component
public class DemoDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final PasswordEncoder passwordEncoder;
    private final TransferService domainTransferService;

    public DemoDataSeeder(UserRepository userRepository,
                          CustomerRepository customerRepository,
                          AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          TransferRepository transferRepository,
                          AccountNumberGenerator accountNumberGenerator,
                          PasswordEncoder passwordEncoder,
                          TransferService domainTransferService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transferRepository = transferRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.passwordEncoder = passwordEncoder;
        this.domainTransferService = domainTransferService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail("admin@intbank.ac.id")) {
            return;
        }

        User admin = userRepository.save(new User(
                "admin@intbank.ac.id", passwordEncoder.encode("Admin123!"), UserRole.ADMIN));

        User budiUser = userRepository.save(new User(
                "budi@intbank.ac.id", passwordEncoder.encode("Nasabah123!"), UserRole.CUSTOMER));
        Customer budi = customerRepository.save(new Customer(
                budiUser.getId(), "Budi Santoso", "081234567890", "Jl. Melati No. 12, Bandung"));

        User sitiUser = userRepository.save(new User(
                "siti@intbank.ac.id", passwordEncoder.encode("Nasabah123!"), UserRole.CUSTOMER));
        Customer siti = customerRepository.save(new Customer(
                sitiUser.getId(), "Siti Rahmawati", "081298765432", "Jl. Anggrek No. 8, Jakarta"));

        Account tabunganBudi = accountRepository.save(new SavingsAccount(
                accountNumberGenerator.generate(), budi.getId(), Money.of(2_500_000)));
        ledger(tabunganBudi, TransactionType.OPEN_ACCOUNT, Money.of(2_500_000),
                Money.ZERO, "Saldo awal simulasi", budiUser.getId());

        Account giroBudi = accountRepository.save(new CurrentAccount(
                accountNumberGenerator.generate(), budi.getId(), Money.of(750_000)));
        ledger(giroBudi, TransactionType.OPEN_ACCOUNT, Money.of(750_000),
                Money.ZERO, "Saldo awal simulasi", budiUser.getId());

        Account depositoBudi = accountRepository.save(new DepositAccount(
                accountNumberGenerator.generate(), budi.getId(), Money.of(5_000_000),
                12, LocalDate.now().minusMonths(3)));
        ledger(depositoBudi, TransactionType.OPEN_ACCOUNT, Money.of(5_000_000),
                Money.ZERO, "Pembukaan deposito 12 bulan", budiUser.getId());

        Money setoran = Money.of(1_000_000);
        tabunganBudi.credit(setoran);
        ledger(tabunganBudi, TransactionType.DEPOSIT, setoran, Money.ZERO,
                "Setor dana simulasi", budiUser.getId());

        Account tabunganSiti = accountRepository.save(new SavingsAccount(
                accountNumberGenerator.generate(), siti.getId(), Money.of(1_200_000)));
        ledger(tabunganSiti, TransactionType.OPEN_ACCOUNT, Money.of(1_200_000),
                Money.ZERO, "Saldo awal simulasi", sitiUser.getId());

        Money transferAmount = Money.of(350_000);
        TransferService.Receipt receipt = domainTransferService.transfer(
                tabunganBudi, tabunganSiti, transferAmount, budi.getId(), LocalDate.now());

        Transaction keluar = transactionRepository.save(new Transaction(
                FormatUtil.reference(), tabunganBudi.id(), TransactionType.TRANSFER_OUT,
                transferAmount, receipt.fee(), receipt.senderBalance(),
                "Transfer ke " + FormatUtil.mask(tabunganSiti.accountNumber()), budiUser.getId()));
        Transaction masuk = transactionRepository.save(new Transaction(
                FormatUtil.reference(), tabunganSiti.id(), TransactionType.TRANSFER_IN,
                transferAmount, Money.ZERO, receipt.receiverBalance(),
                "Transfer dari " + FormatUtil.mask(tabunganBudi.accountNumber()), budiUser.getId()));
        transferRepository.save(new Transfer(
                "TRF" + keluar.getReferenceNumber().substring(3),
                keluar.getId(), masuk.getId(), tabunganBudi.id(), tabunganSiti.id(), transferAmount));
    }

    private void ledger(Account account, TransactionType type, Money amount, Money fee,
                        String description, Long createdBy) {
        transactionRepository.save(new Transaction(
                FormatUtil.reference(), account.id(), type, amount, fee,
                account.balance(), description, createdBy));
    }
}
