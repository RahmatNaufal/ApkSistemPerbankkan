package com.intbank.service;

import com.intbank.domain.Account;
import com.intbank.domain.DomainExceptions.DomainException;
import com.intbank.domain.IdempotencyKey;
import com.intbank.domain.Money;
import com.intbank.domain.Transaction;
import com.intbank.domain.TransactionType;
import com.intbank.domain.Transfer;
import com.intbank.domain.TransferService;
import com.intbank.dto.TransferRequest;
import com.intbank.repository.AccountRepository;
import com.intbank.repository.IdempotencyKeyRepository;
import com.intbank.repository.TransactionRepository;
import com.intbank.repository.TransferRepository;
import com.intbank.util.FormatUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Orkestrasi transfer antarrekening: kunci baris, idempotensi, dan ledger
 * berpasangan. Seluruh aturan uang tetap di {@link TransferService} (domain).
 */
@Service
public class TransferApplicationService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final CurrentUserService currentUserService;
    private final TransactionLimitService limitService;
    private final TransferService domainTransferService;
    private final AuditService auditService;

    public TransferApplicationService(AccountRepository accountRepository,
                                      TransactionRepository transactionRepository,
                                      TransferRepository transferRepository,
                                      IdempotencyKeyRepository idempotencyKeyRepository,
                                      CurrentUserService currentUserService,
                                      TransactionLimitService limitService,
                                      TransferService domainTransferService,
                                      AuditService auditService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transferRepository = transferRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.currentUserService = currentUserService;
        this.limitService = limitService;
        this.domainTransferService = domainTransferService;
        this.auditService = auditService;
    }

    @Transactional
    public String transfer(TransferRequest request) {
        Long userId = currentUserService.currentUserId();
        Long customerId = currentUserService.currentCustomerId();
        String key = request.getIdempotencyKey().trim();

        Optional<IdempotencyKey> previous = idempotencyKeyRepository.findByUserIdAndKey(userId, key);
        if (previous.isPresent()) {
            Long txId = previous.get().getTransactionId();
            if (txId != null) {
                return transactionRepository.findById(txId)
                        .map(Transaction::getReferenceNumber)
                        .orElseThrow(() -> new DomainException("Transaksi sebelumnya tidak ditemukan."));
            }
            throw new DomainException("Permintaan transfer ini sedang diproses.");
        }

        Money amount;
        try {
            amount = FormatUtil.parseAmount(request.getAmount());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Nominal harus berupa angka bulat yang lebih dari nol.");
        }
        limitService.checkAmount(amount);

        Account sumberRef = accountRepository.findByAccountNumber(request.getSourceAccountNumber().trim())
                .orElseThrow(() -> new DomainException("Rekening sumber tidak ditemukan."));
        Account tujuanRef = accountRepository.findByAccountNumber(request.getDestinationAccountNumber().trim())
                .orElseThrow(() -> new DomainException("Rekening tujuan tidak ditemukan."));

        if (sumberRef.id().equals(tujuanRef.id())) {
            throw new com.intbank.domain.DomainExceptions.SameAccountTransferException();
        }
        if (!sumberRef.ownerCustomerId().equals(customerId)) {
            throw new com.intbank.domain.DomainExceptions.AccountAccessDeniedException();
        }

        // Kunci dua baris dalam urutan id menaik agar dua transfer berlawanan tidak deadlock.
        List<Long> ids = new ArrayList<>(List.of(sumberRef.id(), tujuanRef.id()));
        ids.sort(Comparator.naturalOrder());
        List<Account> locked = accountRepository.findAllByIdForUpdate(ids);
        Account sumber = locked.stream().filter(a -> a.id().equals(sumberRef.id())).findFirst().orElse(sumberRef);
        Account tujuan = locked.stream().filter(a -> a.id().equals(tujuanRef.id())).findFirst().orElse(tujuanRef);

        limitService.checkDailyWithdrawal(sumber.id(), amount);
        TransferService.Receipt receipt = domainTransferService.transfer(
                sumber, tujuan, amount, customerId, LocalDate.now());

        String referenceKeluar = FormatUtil.reference();
        String referenceMasuk = FormatUtil.reference();
        Transaction keluar = transactionRepository.save(new Transaction(
                referenceKeluar, sumber.id(), TransactionType.TRANSFER_OUT,
                amount, receipt.fee(), receipt.senderBalance(),
                "Transfer ke " + FormatUtil.mask(tujuan.accountNumber()), userId));
        Transaction masuk = transactionRepository.save(new Transaction(
                referenceMasuk, tujuan.id(), TransactionType.TRANSFER_IN,
                amount, Money.ZERO, receipt.receiverBalance(),
                "Transfer dari " + FormatUtil.mask(sumber.accountNumber()), userId));

        Transfer transfer = transferRepository.save(new Transfer(
                "TRF" + referenceKeluar.substring(3),
                keluar.getId(), masuk.getId(), sumber.id(), tujuan.id(), amount));

        String requestHash = Integer.toHexString(Objects.hash(
                request.getSourceAccountNumber(), request.getDestinationAccountNumber(),
                request.getAmount(), customerId));
        idempotencyKeyRepository.save(new IdempotencyKey(userId, key, requestHash, keluar.getId()));
        auditService.record("TRANSFER", "ACCOUNT", sumber.accountNumber(),
                "Transfer " + FormatUtil.rupiah(amount) + " ke " + tujuan.accountNumber()
                        + " · " + transfer.getTransferReference());
        return referenceKeluar;
    }
}
