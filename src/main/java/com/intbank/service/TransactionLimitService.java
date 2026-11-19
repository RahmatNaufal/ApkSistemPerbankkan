package com.intbank.service;

import com.intbank.domain.DomainExceptions.DomainException;
import com.intbank.domain.Money;
import com.intbank.domain.Transaction;
import com.intbank.domain.TransactionType;
import com.intbank.config.BankingProperties;
import com.intbank.repository.TransactionRepository;
import com.intbank.util.FormatUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Batas nominal transaksi dan total penarikan harian (konfigurasi intbank.*). */
@Service
public class TransactionLimitService {

    private final BankingProperties properties;
    private final TransactionRepository transactionRepository;

    public TransactionLimitService(BankingProperties properties, TransactionRepository transactionRepository) {
        this.properties = properties;
        this.transactionRepository = transactionRepository;
    }

    public void checkAmount(Money amount) {
        if (amount.toBigDecimal().compareTo(properties.maxTransactionAmount()) > 0) {
            throw new DomainException("Nominal melebihi batas transaksi "
                    + FormatUtil.rupiah(Money.of(properties.maxTransactionAmount())) + ".");
        }
    }

    /** Termasuk transfer keluar; dihitung dari ledger hari ini. */
    public void checkDailyWithdrawal(Long accountId, Money amount) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<Transaction> keluar = transactionRepository.findByAccountIdAndTypeInAndCreatedAtAfter(
                accountId, List.of(TransactionType.WITHDRAWAL, TransactionType.TRANSFER_OUT), startOfDay);
        Money total = Money.ZERO;
        for (Transaction t : keluar) {
            total = total.plus(t.getAmount());
        }
        Money maksimal = Money.of(properties.maxDailyWithdrawal());
        if (total.plus(amount).compareTo(maksimal) > 0) {
            throw new DomainException("Total penarikan hari ini melebihi batas "
                    + FormatUtil.rupiah(maksimal) + ".");
        }
    }
}
