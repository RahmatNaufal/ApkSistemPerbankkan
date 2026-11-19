package com.intbank.domain;

/**
 * PEWARISAN: Giro.
 *
 * <p>Tanpa saldo minimum, biaya Rp2.500 per penarikan/transfer keluar,
 * dan tidak menghasilkan bunga.
 */
public class CurrentAccount extends Account {

    /** Konstruktor untuk ORM/JPA. */
    protected CurrentAccount() {
        super();
    }

    public CurrentAccount(String accountNumber, Long ownerCustomerId, Money opening) {
        super(accountNumber, ownerCustomerId, opening);
    }

    @Override
    public String typeName() {
        return "Giro";
    }

    @Override
    protected Money minimumBalance() {
        return Money.ZERO;
    }

    @Override
    protected Money withdrawalFee(Money amount) {
        return BankingRules.CURRENT_ACCOUNT_FEE;
    }

    @Override
    protected Money projectedInterest(int months) {
        return Money.ZERO;
    }
}
