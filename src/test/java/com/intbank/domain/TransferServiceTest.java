package com.intbank.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.intbank.domain.DomainExceptions.*;
import static org.junit.jupiter.api.Assertions.*;

/** Unit test alur transfer: validasi sebelum mutasi, kegagalan tidak mengubah saldo. */
class TransferServiceTest {

    private static final LocalDate HARI_INI = LocalDate.of(2026, 11, 19);
    private final TransferService service = new TransferService();

    @Test
    @DisplayName("Transfer valid: pengirim dan penerima berubah sesuai nominal")
    void transferValid() {
        Account pengirim = new SavingsAccount("1101", 10L, Money.of(500_000));
        Account penerima = new CurrentAccount("2101", 20L, Money.of(100_000));

        TransferService.Receipt struk = service.transfer(pengirim, penerima, Money.of(200_000), 10L, HARI_INI);

        assertEquals(Money.of(200_000), struk.amount());
        assertEquals(Money.ZERO, struk.fee());
        assertEquals(Money.of(300_000), struk.senderBalance());
        assertEquals(Money.of(300_000), struk.receiverBalance());
    }

    @Test
    @DisplayName("Transfer dari Giro memotong biaya, penerima tetap menerima nominal penuh")
    void transferDariGiroMemotongBiaya() {
        Account pengirim = new CurrentAccount("2101", 20L, Money.of(300_000));
        Account penerima = new SavingsAccount("1101", 10L, Money.of(100_000));

        TransferService.Receipt struk = service.transfer(pengirim, penerima, Money.of(100_000), 20L, HARI_INI);

        assertEquals(Money.of(2_500), struk.fee());
        assertEquals(Money.of(197_500), pengirim.balance());
        assertEquals(Money.of(200_000), penerima.balance());
    }

    @Test
    @DisplayName("Transfer ke rekening sendiri ditolak")
    void transferKeDiriSendiriDitolak() {
        Account rekening = new SavingsAccount("1101", 10L, Money.of(500_000));
        assertThrows(SameAccountTransferException.class,
                () -> service.transfer(rekening, rekening, Money.of(1), 10L, HARI_INI));
    }

    @Test
    @DisplayName("Transfer dari rekening milik orang lain ditolak (anti-IDOR)")
    void transferMilikOrangLainDitolak() {
        Account pengirim = new SavingsAccount("1101", 10L, Money.of(500_000));
        Account penerima = new CurrentAccount("2101", 20L, Money.of(100_000));
        assertThrows(AccountAccessDeniedException.class,
                () -> service.transfer(pengirim, penerima, Money.of(1), 99L, HARI_INI));
    }

    @Test
    @DisplayName("Transfer melebihi saldo ditolak tanpa mengubah saldo")
    void transferMelebihiSaldoDitolak() {
        Account pengirim = new SavingsAccount("1101", 10L, Money.of(500_000));
        Account penerima = new CurrentAccount("2101", 20L, Money.of(100_000));

        assertThrows(InsufficientBalanceException.class,
                () -> service.transfer(pengirim, penerima, Money.of(9_999_999), 10L, HARI_INI));
        assertEquals(Money.of(500_000), pengirim.balance());
        assertEquals(Money.of(100_000), penerima.balance());
    }

    @Test
    @DisplayName("Transfer ke rekening nonaktif ditolak")
    void transferKeRekeningNonaktifDitolak() {
        Account pengirim = new SavingsAccount("1101", 10L, Money.of(500_000));
        Account penerima = new CurrentAccount("2101", 20L, Money.of(100_000));
        penerima.deactivate();

        assertThrows(InactiveAccountException.class,
                () -> service.transfer(pengirim, penerima, Money.of(1_000), 10L, HARI_INI));
        assertEquals(Money.of(500_000), pengirim.balance());
    }

    @Test
    @DisplayName("Transfer ke Deposito ditolak")
    void transferKeDepositoDitolak() {
        Account pengirim = new SavingsAccount("1101", 10L, Money.of(500_000));
        Account deposito = new DepositAccount("3001", 20L, Money.of(1_000_000), 6, HARI_INI);

        assertThrows(DomainException.class,
                () -> service.transfer(pengirim, deposito, Money.of(10_000), 10L, HARI_INI));
        assertEquals(Money.of(500_000), pengirim.balance());
    }

    @Test
    @DisplayName("Nominal nol dan negatif ditolak")
    void nominalInvalidDitolak() {
        Account pengirim = new SavingsAccount("1101", 10L, Money.of(500_000));
        Account penerima = new CurrentAccount("2101", 20L, Money.of(100_000));

        assertThrows(InvalidAmountException.class,
                () -> service.transfer(pengirim, penerima, Money.ZERO, 10L, HARI_INI));
        assertThrows(InvalidAmountException.class,
                () -> service.transfer(pengirim, penerima, Money.of(-1), 10L, HARI_INI));
    }
}
