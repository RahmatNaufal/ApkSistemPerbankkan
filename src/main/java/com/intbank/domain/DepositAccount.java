package com.intbank.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;

import static com.intbank.domain.DomainExceptions.DomainException;
import static com.intbank.domain.DomainExceptions.WithdrawalNotAllowedException;

/**
 * PEWARISAN: Deposito berjangka.
 *
 * <p>Tidak menerima setoran tambahan (hanya dana awal) dan hanya bisa dicairkan
 * <b>penuh</b> setelah jatuh tempo. Penolakan ini memang bagian dari kontrak
 * {@link Account#withdraw(Money, LocalDate)}, jadi tidak melanggar LSP.
 */
@Entity
@DiscriminatorValue("DEPOSIT")
public class DepositAccount extends Account {

    @Column(name = "tenor_months")
    private int tenorMonths;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    /** Konstruktor untuk ORM/JPA. */
    protected DepositAccount() {
        super();
    }

    public DepositAccount(String accountNumber, Long ownerCustomerId, Money principal,
                          int tenorMonths, LocalDate openedOn) {
        super(accountNumber, ownerCustomerId, principal);
        if (tenorMonths <= 0) {
            throw new DomainException("Tenor harus positif.");
        }
        if (openedOn == null) {
            throw new IllegalArgumentException("Tanggal pembukaan wajib diisi.");
        }
        this.tenorMonths = tenorMonths;
        this.maturityDate = openedOn.plusMonths(tenorMonths);
    }

    public LocalDate maturityDate() {
        return maturityDate;
    }

    public int tenorMonths() {
        return tenorMonths;
    }

    @Override
    public String typeName() {
        return "Deposito";
    }

    @Override
    protected Money minimumBalance() {
        return Money.ZERO;
    }

    @Override
    protected Money withdrawalFee(Money amount) {
        return Money.ZERO;
    }

    @Override
    protected Money projectedInterest(int months) {
        return interest(balance(), BankingRules.DEPOSIT_ANNUAL_RATE, Math.min(months, tenorMonths));
    }

    @Override
    protected void ensureCanCredit(Money amount) {
        throw new DomainException("Deposito tidak menerima setoran tambahan.");
    }

    @Override
    protected void ensureCanWithdraw(Money amount, Money total, LocalDate today) {
        if (maturityDate == null || today.isBefore(maturityDate)) {
            throw new WithdrawalNotAllowedException(
                    "Deposito baru dapat dicairkan pada " + maturityDate + ".");
        }
        if (amount.compareTo(balance()) != 0) {
            throw new WithdrawalNotAllowedException("Deposito hanya dapat dicairkan penuh.");
        }
    }
}
