package com.intbank.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.intbank.domain.DomainExceptions.*;
import static org.junit.jupiter.api.Assertions.*;

/** Unit test untuk ENKAPSULASI saldo dan state machine status. */
class AccountTest {

    private static final LocalDate HARI_INI = LocalDate.of(2026, 11, 19);

    private SavingsAccount tabungan() {
        return new SavingsAccount("1000000001", 1L, Money.of(1_000_000));
    }

    @Test
    @DisplayName("Kredit nominal nol ditolak")
    void kreditNolDitolak() {
        assertThrows(InvalidAmountException.class, () -> tabungan().credit(Money.ZERO));
    }

    @Test
    @DisplayName("Kredit nominal negatif ditolak")
    void kreditNegatifDitolak() {
        assertThrows(InvalidAmountException.class, () -> tabungan().credit(Money.of(-5)));
    }

    @Test
    @DisplayName("Saldo tidak berubah setelah penolakan")
    void saldoTidakBerubahSetelahPenolakan() {
        SavingsAccount rekening = tabungan();
        assertThrows(InvalidAmountException.class, () -> rekening.credit(Money.ZERO));
        assertEquals(Money.of(1_000_000), rekening.balance());
    }

    @Test
    @DisplayName("Tarik dari Tabungan tanpa biaya")
    void tarikTabunganTanpaBiaya() {
        SavingsAccount rekening = tabungan();
        assertEquals(Money.ZERO, rekening.withdraw(Money.of(100_000), HARI_INI));
        assertEquals(Money.of(900_000), rekening.balance());
    }

    @Test
    @DisplayName("Tarik dari Giro memotong biaya Rp2.500")
    void tarikGiroMemotongBiaya() {
        CurrentAccount rekening = new CurrentAccount("2000000001", 1L, Money.of(1_000_000));
        assertEquals(Money.of(2_500), rekening.withdraw(Money.of(100_000), HARI_INI));
        assertEquals(Money.of(897_500), rekening.balance());
    }

    @Test
    @DisplayName("Tabungan menjaga saldo minimum Rp50.000")
    void tabunganMenjagaSaldoMinimum() {
        SavingsAccount rekening = tabungan();
        assertThrows(InsufficientBalanceException.class,
                () -> rekening.withdraw(Money.of(960_000), HARI_INI));
        assertEquals(Money.of(1_000_000), rekening.balance());
    }

    @Test
    @DisplayName("Giro boleh kosong, tetapi biaya ikut dihitung")
    void giroBolehKosong() {
        CurrentAccount rekening = new CurrentAccount("2000000002", 1L, Money.of(100_000));
        assertThrows(InsufficientBalanceException.class,
                () -> rekening.withdraw(Money.of(100_000), HARI_INI));
        assertEquals(Money.of(100_000), rekening.balance());
    }

    @Test
    @DisplayName("canWithdraw hanya memeriksa, tidak mengubah saldo")
    void canWithdrawTidakMengubahSaldo() {
        SavingsAccount rekening = tabungan();
        assertTrue(rekening.canWithdraw(Money.of(100_000), HARI_INI));
        assertFalse(rekening.canWithdraw(Money.of(999_999), HARI_INI));
        assertEquals(Money.of(1_000_000), rekening.balance());
    }

    @Test
    @DisplayName("Rekening nonaktif menolak setor dan tarik")
    void rekeningNonaktifMenolakTransaksi() {
        SavingsAccount rekening = tabungan();
        rekening.deactivate();

        assertEquals(AccountStatus.INACTIVE, rekening.status());
        assertThrows(InactiveAccountException.class, () -> rekening.credit(Money.of(10_000)));
        assertThrows(InactiveAccountException.class, () -> rekening.withdraw(Money.of(10_000), HARI_INI));
    }

    @Test
    @DisplayName("Transisi status mengikuti aturan")
    void transisiStatus() {
        SavingsAccount rekening = new SavingsAccount("1000000002", 1L, Money.ZERO);

        rekening.deactivate();
        assertEquals(AccountStatus.INACTIVE, rekening.status());
        rekening.activate();
        assertEquals(AccountStatus.ACTIVE, rekening.status());
        rekening.block();
        assertEquals(AccountStatus.BLOCKED, rekening.status());
        rekening.activate();
        assertEquals(AccountStatus.ACTIVE, rekening.status());
        rekening.close();
        assertEquals(AccountStatus.CLOSED, rekening.status());

        assertThrows(DomainException.class, rekening::activate);
        assertThrows(DomainException.class, rekening::deactivate);
        assertThrows(DomainException.class, rekening::block);
    }

    @Test
    @DisplayName("Rekening BLOCKED tidak bisa langsung dinonaktifkan")
    void blockedTidakBisaDeactivate() {
        SavingsAccount rekening = tabungan();
        rekening.block();

        assertThrows(DomainException.class, rekening::deactivate);
        assertEquals(AccountStatus.BLOCKED, rekening.status());

        rekening.activate();
        assertEquals(AccountStatus.ACTIVE, rekening.status());
    }

    @Test
    @DisplayName("Rekening hanya bisa ditutup jika saldo nol")
    void closeDitolakJikaSaldoTidakNol() {
        SavingsAccount rekening = tabungan();
        assertThrows(DomainException.class, rekening::close);
        assertEquals(AccountStatus.ACTIVE, rekening.status());
    }
}
