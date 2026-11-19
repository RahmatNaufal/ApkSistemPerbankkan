package com.intbank.domain;

/**
 * PEWARISAN: Tabungan.
 *
 * <p>Saldo minimum Rp50.000, tanpa biaya tarik/transfer, bunga proyeksi
 * 3% per tahun. Aturan umum tetap di {@link Account}; kelas ini hanya
 * mengisi hook yang berbeda.
 */
public class SavingsAccount extends Account {

    /** Konstruktor untuk ORM/JPA. */
    protected SavingsAccount() {
        super();
    }

    public SavingsAccount(String accountNumber, Long ownerCustomerId, Money opening) {
        super(accountNumber, ownerCustomerId, opening);
    }

    @Override
    public String typeName() {
        return "Tabungan";
    }

    @Override
    protected Money minimumBalance() {
        return BankingRules.MINIMUM_SAVINGS_BALANCE;
    }

    @Override
    protected Money withdrawalFee(Money amount) {
        return Money.ZERO;
    }

    @Override
    protected Money projectedInterest(int months) {
        return interest(balance(), BankingRules.SAVINGS_ANNUAL_RATE, months);
    }
}
