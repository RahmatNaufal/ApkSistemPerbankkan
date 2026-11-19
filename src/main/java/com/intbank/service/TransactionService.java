package com.intbank.service;

import com.intbank.domain.Transaction;
import com.intbank.domain.TransactionStatus;
import com.intbank.domain.TransactionType;
import com.intbank.domain.Transfer;
import com.intbank.repository.AccountRepository;
import com.intbank.repository.TransactionRepository;
import com.intbank.repository.TransferRepository;
import com.intbank.web.ViewMapper;
import com.intbank.web.Views.TransactionView;
import com.intbank.web.Views.TransferView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Lampiran transaksi: riwayat berpaginasi, bukti transaksi, dan filter. */
@Service
@Transactional(readOnly = true)
public class TransactionService {

    private static final int PAGE_SIZE = 10;

    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final ViewMapper viewMapper;

    public TransactionService(TransactionRepository transactionRepository,
                              TransferRepository transferRepository,
                              AccountRepository accountRepository,
                              ViewMapper viewMapper) {
        this.transactionRepository = transactionRepository;
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
        this.viewMapper = viewMapper;
    }

    public List<TransactionView> recentTransactions(Collection<Long> accountIds, int limit) {
        if (accountIds.isEmpty()) {
            return List.of();
        }
        return transactionRepository.findByAccountIdInOrderByCreatedAtDesc(accountIds).stream()
                .limit(limit).map(viewMapper::toTransactionView).toList();
    }

    public List<TransactionView> forAccount(Long accountId) {
        return viewMapper.toTransactionViews(
                transactionRepository.findByAccountIdInOrderByCreatedAtDesc(List.of(accountId)));
    }

    /** Semua transaksi terakhir lintas rekening (dipakai dashboard admin). */
    public List<TransactionView> latest(int limit) {
        return transactionRepository.findAll(PageRequest.of(0, limit,
                Sort.by(Sort.Direction.DESC, "createdAt"))).stream()
                .map(viewMapper::toTransactionView).toList();
    }

    public Page<TransactionView> historyForCustomer(Collection<Long> accountIds, String keyword,
                                                    String type, String status,
                                                    String fromDate, String toDate, int page) {
        Pageable pageable = pageable(page);
        if (accountIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return transactionRepository.searchForCustomer(accountIds,
                        parseType(type), parseStatus(status), blankToNull(keyword),
                        parseFrom(fromDate), parseTo(toDate), pageable)
                .map(viewMapper::toTransactionView);
    }

    public Page<TransactionView> historyForAdmin(String keyword, String type, String status,
                                                 String fromDate, String toDate, int page) {
        return transactionRepository.search(null,
                        parseType(type), parseStatus(status), blankToNull(keyword),
                        parseFrom(fromDate), parseTo(toDate), pageable(page))
                .map(viewMapper::toTransactionView);
    }

    public TransactionView byReference(String reference) {
        return transactionRepository.findByReferenceNumber(reference)
                .map(viewMapper::toTransactionView)
                .orElseThrow(() -> new com.intbank.domain.DomainExceptions.DomainException(
                        "Transaksi tidak ditemukan."));
    }

    /** Mengembalikan entity (dipakai controller untuk pengecekan kepemilikan). */
    public Transaction entityByReference(String reference) {
        return transactionRepository.findByReferenceNumber(reference)
                .orElseThrow(() -> new com.intbank.domain.DomainExceptions.DomainException(
                        "Transaksi tidak ditemukan."));
    }

    public TransferView transferFor(Transaction transaction) {
        if (transaction.getType() == TransactionType.TRANSFER_OUT) {
            return transferRepository.findByTransactionOutId(transaction.getId())
                    .map(viewMapper::toTransferView).orElse(null);
        }
        if (transaction.getType() == TransactionType.TRANSFER_IN) {
            return transferRepository.findByTransactionInId(transaction.getId())
                    .map(viewMapper::toTransferView).orElse(null);
        }
        return null;
    }

    public Long accountIdByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(com.intbank.domain.Account::id).orElse(null);
    }

    private Pageable pageable(int page) {
        return PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private TransactionType parseType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return TransactionType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private TransactionStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return TransactionStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private LocalDateTime parseFrom(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim()).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseTo(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim()).atTime(LocalTime.MAX);
        } catch (Exception e) {
            return null;
        }
    }
}
