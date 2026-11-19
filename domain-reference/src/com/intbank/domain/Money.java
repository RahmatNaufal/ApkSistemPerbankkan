package com.intbank.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value object uang (IDR, tanpa pecahan).
 *
 * <p><b>ENKAPSULASI:</b> nilai disimpan private dan tidak bisa diubah
 * (immutable). Tidak ada setter; setiap operasi mengembalikan objek {@code Money}
 * baru. Dengan begitu saldo tidak pernah berubah secara tidak sengaja dari luar.
 *
 * <p>Seluruh perhitungan memakai {@link BigDecimal}, bukan {@code double}.
 */
public final class Money implements Comparable<Money> {

    /** Konstanta nol yang bisa dipakai bersama (immutable). */
    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal value;

    private Money(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Nilai uang tidak boleh null.");
        }
        this.value = value.setScale(0, RoundingMode.DOWN);
    }

    /** Membuat uang dari jumlah rupiah bulat. */
    public static Money of(long rupiah) {
        return new Money(BigDecimal.valueOf(rupiah));
    }

    /** Membuat uang dari BigDecimal; pecahan dibulatkan ke bawah. */
    public static Money of(BigDecimal value) {
        return new Money(value);
    }

    public Money plus(Money other) {
        return new Money(value.add(other.value));
    }

    public Money minus(Money other) {
        return new Money(value.subtract(other.value));
    }

    /** Hasil kali dengan faktor pecahan, dibulatkan ke bawah (misalnya bunga). */
    public Money times(BigDecimal factor) {
        return new Money(value.multiply(factor));
    }

    public boolean isPositive() {
        return value.signum() > 0;
    }

    public boolean isNegative() {
        return value.signum() < 0;
    }

    public boolean isZero() {
        return value.signum() == 0;
    }

    public BigDecimal toBigDecimal() {
        return value;
    }

    @Override
    public int compareTo(Money other) {
        return value.compareTo(other.value);
    }

    /** Dua Money sama jika nilai rupiahnya sama, walaupun scale berbeda. */
    @Override
    public boolean equals(Object other) {
        return other instanceof Money money && value.compareTo(money.value) == 0;
    }

    @Override
    public int hashCode() {
        return value.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return "Rp" + String.format("%,d", value.longValue()).replace(',', '.');
    }
}
