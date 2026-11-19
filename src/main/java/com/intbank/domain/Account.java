package com.intbank.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.intbank.domain.DomainExceptions.*;

/**
 * Kontrak umum rekening (ABSTRAKSI).
 *
 * <p>Aturan yang sama untuk semua rekening (nominal harus positif, rekening
 * harus aktif, saldo tidak boleh negatif) ditulis sekali di sini sebagai
 * <i>template method</i>. Aturan yang berbeda diserahkan ke subclass lewat
 * method hook.
 *
 * <p><b>ENKAPSULASI:</b> {@code balance} dan {@code status} private tanpa
 * setter publik. Satu-satunya jalan mengubah saldo adalah {@link #credit(Money)}
 * dan {@link #withdraw(Money, LocalDate)} yang selalu memvalidasi invariant.
 *
 * <p><b>Kontrak {@code withdraw()}:</b> penarikan <i>boleh ditolak</i> sesuai
 * aturan jenis rekening dengan melempar {@link DomainException}. Karena kontrak
 * ini tertulis di kelas induk, {@link DepositAccount} yang menolak penarikan
 * sebelum jatuh tempo tidak melanggar Liskov Substitution Principle (LSP).
 * UI bisa memakai {@link #canWithdraw(Money, LocalDate)} tanpa {@code instanceof}.
 */
@Entity
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    @Column(name = "account_number", nullable = false, length = 10, unique = true)
    private String accountNumber;

    @Column(name = "customer_id", nullable = false)
    private Long ownerCustomerId;

    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false, precision = 19, scale = 0)
    private Money balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Konstruktor tanpa argumen yang dilindungi. Diperlukan ORM/JPA kelak,
     * tetapi tidak bisa dipanggil dari kode sembarangan.
     */
    protected Account() {
        this.balance = Money.ZERO;
    }

    protected Account(String accountNumber, Long ownerCustomerId, Money opening) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Nomor rekening wajib diisi.");
        }
        if (ownerCustomerId == null) {
            throw new IllegalArgumentException("Pemilik rekening wajib diisi.");
        }
        if (opening == null || opening.isNegative()) {
            throw new InvalidAmountException();
        }
        this.accountNumber = accountNumber;
        this.ownerCustomerId = ownerCustomerId;
        this.balance = opening;
    }

    @PrePersist
    void onPersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ---- getter saja (read-only) ----

    public Long id() {
        return id;
    }

    public long version() {
        return version;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public String accountNumber() {
        return accountNumber;
    }

    public Long ownerCustomerId() {
        return ownerCustomerId;
    }

    public Money balance() {
        return balance;
    }

    public AccountStatus status() {
        return status;
    }

    // ---- operasi domain: urutan tetap, langkah bisa berbeda per jenis ----

    /** Menambah dana. Memvalidasi lebih dulu, baru mengubah saldo. */
    public final void credit(Money amount) {
        validateCredit(amount);
        balance = balance.plus(amount);
    }

    /**
     * Mengurangi dana.
     *
     * @return biaya yang ikut dipotong ({@link Money#ZERO} jika tidak ada biaya).
     */
    public final Money withdraw(Money amount, LocalDate today) {
        validateWithdraw(amount, today);
        Money fee = withdrawalFee(amount);
        balance = balance.minus(amount.plus(fee));
        return fee;
    }

    /** Melempar exception spesifik jika penarikan tidak diizinkan. Tidak mengubah state. */
    public final void validateWithdraw(Money amount, LocalDate today) {
        requireValidAmount(amount);
        requireActive();
        Money total = amount.plus(withdrawalFee(amount));
        ensureCanWithdraw(amount, total, today); // <- POLIMORFISME
        if (balance.minus(total).compareTo(minimumBalance()) < 0) {
            throw new InsufficientBalanceException(
                    "Saldo tidak mencukupi (saldo minimum " + minimumBalance() + ").");
        }
    }

    /** Melempar exception jika rekening tidak bisa menerima dana. Tidak mengubah state. */
    public final void validateCredit(Money amount) {
        requireValidAmount(amount);
        requireActive();
        ensureCanCredit(amount); // <- POLIMORFISME
    }

    /** Untuk UI: tombol aksi dinonaktifkan jika false. Tidak mengubah state. */
    public final boolean canWithdraw(Money amount, LocalDate today) {
        try {
            validateWithdraw(amount, today);
            return true;
        } catch (DomainException e) {
            return false;
        }
    }

    // ---- state machine status ----

    public void activate() {
        requireNotClosed();
        status = AccountStatus.ACTIVE;
    }

    public void deactivate() {
        requireNotClosed();
        // Sesuai PRD: ACTIVE <-> INACTIVE. BLOCKED hanya bisa kembali ke ACTIVE.
        if (status != AccountStatus.ACTIVE) {
            throw new DomainException("Hanya rekening ACTIVE yang dapat dinonaktifkan.");
        }
        status = AccountStatus.INACTIVE;
    }

    public void block() {
        requireNotClosed();
        status = AccountStatus.BLOCKED;
    }

    public void close() {
        requireNotClosed();
        if (!balance.isZero()) {
            throw new DomainException("Rekening hanya dapat ditutup jika saldo nol.");
        }
        status = AccountStatus.CLOSED;
    }

    // ---- hook yang wajib / boleh diubah subclass ----

    /** Nama jenis rekening untuk ditampilkan di UI. */
    public abstract String typeName();

    protected abstract Money minimumBalance();

    protected abstract Money withdrawalFee(Money amount);

    protected abstract Money projectedInterest(int months);

    /** Aturan tambahan penarikan; secara default tidak ada. */
    protected void ensureCanWithdraw(Money amount, Money total, LocalDate today) {
        // default: tidak ada pembatasan tambahan
    }

    /** Aturan tambahan setoran; secara default tidak ada. */
    protected void ensureCanCredit(Money amount) {
        // default: tidak ada pembatasan tambahan
    }

    /** Proyeksi bunga saja; tidak diposting ke saldo (sesuai batas MVP). */
    public final Money interestProjection(int months) {
        return projectedInterest(months);
    }

    // ---- helper ----

    private void requireValidAmount(Money amount) {
        if (amount == null || !amount.isPositive()) {
            throw new InvalidAmountException();
        }
    }

    private void requireActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new InactiveAccountException();
        }
    }

    private void requireNotClosed() {
        if (status == AccountStatus.CLOSED) {
            throw new DomainException("Rekening berstatus CLOSED tidak dapat diubah.");
        }
    }

    /** Bunga dihitung penuh dengan BigDecimal, dibulatkan sekali di akhir. */
    protected static Money interest(Money base, BigDecimal annualRate, int months) {
        if (months <= 0) {
            return Money.ZERO;
        }
        BigDecimal raw = base.toBigDecimal()
                .multiply(annualRate)
                .multiply(BigDecimal.valueOf(months))
                .divide(BigDecimal.valueOf(12), 0, RoundingMode.DOWN);
        return Money.of(raw);
    }
}
