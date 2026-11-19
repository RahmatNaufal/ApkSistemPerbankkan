import com.intbank.domain.*;

import java.time.LocalDate;

import static com.intbank.domain.DomainExceptions.*;

/**
 * Demo mandiri tanpa library: 22 pemeriksaan sesuai bukti pada REVIEW.md.
 * Jalankan dengan javac/java (lihat README.md) atau lewat skrip demo/jalankan-demo.bat.
 */
public class DomainDemo {
    static int pass = 0;

    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) {
            System.out.println("== GAGAL ==");
            System.exit(1);
        }
        pass++;
    }

    static boolean throwsEx(Class<? extends Throwable> c, Runnable r) {
        try {
            r.run();
            return false;
        } catch (Throwable t) {
            return c.isInstance(t);
        }
    }

    public static void main(String[] args) {
        LocalDate today = LocalDate.of(2026, 11, 19);
        var sav = new SavingsAccount("1001", 1L, Money.of(1_000_000));
        var cur = new CurrentAccount("2001", 2L, Money.of(1_000_000));
        var dep = new DepositAccount("3001", 1L, Money.of(5_000_000), 6, today);

        // Enkapsulasi
        check("credit nominal 0 ditolak", throwsEx(InvalidAmountException.class, () -> sav.credit(Money.ZERO)));
        check("credit nominal negatif ditolak", throwsEx(InvalidAmountException.class, () -> sav.credit(Money.of(-5))));
        check("saldo tidak berubah setelah penolakan", sav.balance().equals(Money.of(1_000_000)));

        // Polimorfisme: referensi bertipe Account, hasil beda per jenis
        Account[] all = {sav, cur, dep};
        Money[] fees = new Money[3];
        fees[0] = sav.withdraw(Money.of(100_000), today);
        fees[1] = cur.withdraw(Money.of(100_000), today);
        check("Tabungan: tanpa biaya, saldo 900.000", fees[0].equals(Money.ZERO) && sav.balance().equals(Money.of(900_000)));
        check("Giro: biaya 2.500, saldo 897.500", fees[1].equals(Money.of(2_500)) && cur.balance().equals(Money.of(897_500)));
        check("Tabungan: saldo minimum 50.000 dijaga",
                throwsEx(InsufficientBalanceException.class, () -> sav.withdraw(Money.of(860_000), today)));
        check("Giro boleh sampai 0 (biaya ikut dihitung)",
                throwsEx(InsufficientBalanceException.class, () -> cur.withdraw(Money.of(897_000), today)));
        check("Deposito belum jatuh tempo ditolak",
                throwsEx(WithdrawalNotAllowedException.class, () -> dep.withdraw(Money.of(5_000_000), today)));
        check("Deposito tidak menerima setoran",
                throwsEx(DomainException.class, () -> dep.credit(Money.of(1))));
        check("Deposito cair penuh setelah jatuh tempo",
                dep.withdraw(Money.of(5_000_000), dep.maturityDate()).equals(Money.ZERO) && dep.balance().equals(Money.ZERO));
        check("canWithdraw tidak mengubah state", sav.canWithdraw(Money.of(1), today) && sav.balance().equals(Money.of(900_000)));

        // Proyeksi bunga polimorfik (tidak diposting)
        var dep2 = new DepositAccount("3002", 1L, Money.of(12_000_000), 12, today);
        check("Bunga deposito 5% p.a. 12 bln = 600.000", dep2.interestProjection(12).equals(Money.of(600_000)));
        check("Bunga tabungan 3% p.a. 12 bln dari 900.000 = 27.000", sav.interestProjection(12).equals(Money.of(27_000)));
        check("Giro tanpa bunga", cur.interestProjection(12).equals(Money.ZERO));

        // Transfer
        var ts = new TransferService();
        var s = new SavingsAccount("1101", 10L, Money.of(500_000));
        var c = new CurrentAccount("2101", 20L, Money.of(100_000));
        var r = ts.transfer(s, c, Money.of(200_000), 10L, today);
        check("Transfer Tabungan->Giro: 300.000 / 300.000",
                r.senderBalance().equals(Money.of(300_000)) && r.receiverBalance().equals(Money.of(300_000)));
        var r2 = ts.transfer(c, s, Money.of(100_000), 20L, today);
        check("Transfer dari Giro memotong biaya 2.500 (pengirim 197.500, penerima 400.000)",
                r2.fee().equals(Money.of(2_500)) && c.balance().equals(Money.of(197_500)) && s.balance().equals(Money.of(400_000)));
        Money sb = s.balance(), cb = c.balance();
        check("Transfer ke diri sendiri ditolak",
                throwsEx(SameAccountTransferException.class, () -> ts.transfer(s, s, Money.of(1), 10L, today)));
        check("Transfer rekening milik orang lain ditolak",
                throwsEx(AccountAccessDeniedException.class, () -> ts.transfer(s, c, Money.of(1), 99L, today)));
        check("Transfer melebihi saldo ditolak",
                throwsEx(InsufficientBalanceException.class, () -> ts.transfer(s, c, Money.of(9_999_999), 10L, today)));
        c.block();
        check("Transfer ke rekening BLOCKED ditolak",
                throwsEx(InactiveAccountException.class, () -> ts.transfer(s, c, Money.of(1_000), 10L, today)));
        check("Kegagalan tidak mengubah saldo siapa pun", s.balance().equals(sb) && c.balance().equals(cb));
        var z = new SavingsAccount("1201", 1L, Money.of(10));
        check("Tutup rekening ditolak jika saldo > 0", throwsEx(DomainException.class, z::close));

        System.out.println("\n" + pass + " pemeriksaan lulus.");
    }
}
