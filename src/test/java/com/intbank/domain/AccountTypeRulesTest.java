package com.intbank.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.intbank.domain.DomainExceptions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test untuk PEWARISAN dan POLIMORFISME:
 * satu panggilan method di tipe {@link Account}, hasil berbeda per jenis.
 */
class AccountTypeRulesTest {

    private static final LocalDate HARI_INI = LocalDate.of(2026, 11, 19);

    @Test
    @DisplayName("Nama jenis rekening berbeda sesuai subclass")
    void namaJenisRekening() {
        assertEquals("Tabungan", new SavingsAccount("1", 1L, Money.ZERO).typeName());
        assertEquals("Giro", new CurrentAccount("2", 1L, Money.ZERO).typeName());
        assertEquals("Deposito", new DepositAccount("3", 1L, Money.of(1_000_000), 3, HARI_INI).typeName());
    }

    @Test
    @DisplayName("Polimorfisme: biaya dan saldo akhir berbeda lewat tipe Account")
    void polimorfismeBiaya() {
        Account tabungan = new SavingsAccount("1001", 1L, Money.of(1_000_000));
        Account giro = new CurrentAccount("2001", 1L, Money.of(1_000_000));

        Money biayaTabungan = tabungan.withdraw(Money.of(100_000), HARI_INI);
        Money biayaGiro = giro.withdraw(Money.of(100_000), HARI_INI);

        assertEquals(Money.ZERO, biayaTabungan);
        assertEquals(Money.of(900_000), tabungan.balance());
        assertEquals(Money.of(2_500), biayaGiro);
        assertEquals(Money.of(897_500), giro.balance());
    }

    @Test
    @DisplayName("Proyeksi bunga berbeda per jenis dan tidak mengubah saldo")
    void proyeksiBunga() {
        SavingsAccount tabungan = new SavingsAccount("1002", 1L, Money.of(900_000));
        CurrentAccount giro = new CurrentAccount("2002", 1L, Money.of(900_000));
        DepositAccount deposito = new DepositAccount("3002", 1L, Money.of(12_000_000), 12, HARI_INI);

        assertEquals(Money.of(27_000), tabungan.interestProjection(12));
        assertEquals(Money.ZERO, giro.interestProjection(12));
        assertEquals(Money.of(600_000), deposito.interestProjection(12));

        assertEquals(Money.of(900_000), tabungan.balance());
        assertEquals(Money.of(900_000), giro.balance());
        assertEquals(Money.of(12_000_000), deposito.balance());
    }

    @Test
    @DisplayName("Deposito menolak setoran tambahan")
    void depositoMenolakSetoran() {
        DepositAccount deposito = baru();
        assertThrows(DomainException.class, () -> deposito.credit(Money.of(1)));
        assertEquals(Money.of(5_000_000), deposito.balance());
    }

    @Test
    @DisplayName("Deposito belum jatuh tempo ditolak")
    void depositoBelumJatuhTempoDitolak() {
        DepositAccount deposito = baru();
        assertThrows(WithdrawalNotAllowedException.class,
                () -> deposito.withdraw(Money.of(5_000_000), HARI_INI));
        assertEquals(Money.of(5_000_000), deposito.balance());
    }

    @Test
    @DisplayName("Deposito cair penuh setelah jatuh tempo")
    void depositoCairPenuhSetelahJatuhTempo() {
        DepositAccount deposito = baru();
        LocalDate jatuhTempo = deposito.maturityDate();

        assertEquals(6, deposito.tenorMonths());
        assertEquals(HARI_INI.plusMonths(6), jatuhTempo);
        assertTrue(deposito.canWithdraw(Money.of(5_000_000), jatuhTempo));
        assertEquals(Money.ZERO, deposito.withdraw(Money.of(5_000_000), jatuhTempo));
        assertEquals(Money.ZERO, deposito.balance());
    }

    @Test
    @DisplayName("Deposito tidak boleh dicairkan sebagian")
    void depositoTidakBolehSebagian() {
        DepositAccount deposito = baru();
        assertThrows(WithdrawalNotAllowedException.class,
                () -> deposito.withdraw(Money.of(1_000_000), deposito.maturityDate()));
    }

    @Test
    @DisplayName("Tenor deposito harus positif")
    void tenorHarusPositif() {
        assertThrows(DomainException.class,
                () -> new DepositAccount("3003", 1L, Money.of(1_000_000), 0, HARI_INI));
    }

    private DepositAccount baru() {
        return new DepositAccount("3001", 1L, Money.of(5_000_000), 6, HARI_INI);
    }
}
