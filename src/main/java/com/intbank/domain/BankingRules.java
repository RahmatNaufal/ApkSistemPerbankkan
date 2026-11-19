package com.intbank.domain;

import java.math.BigDecimal;

/**
 * Nilai aturan bisnis demo yang dipusatkan di satu tempat.
 *
 * <p>Tahap ini masih berupa konstanta domain murni. Saat lapisan Spring Boot
 * dibuat, nilai yang sama dapat dipindahkan ke {@code BankingProperties}
 * (@ConfigurationProperties) dan disuntikkan ke service tanpa mengubah
 * aturan di kelas rekening.
 */
public final class BankingRules {

    /** Saldo minimum rekening Tabungan. */
    public static final Money MINIMUM_SAVINGS_BALANCE = Money.of(50_000);

    /** Biaya setiap penarikan/transfer keluar dari Giro. */
    public static final Money CURRENT_ACCOUNT_FEE = Money.of(2_500);

    /** Bunga tahunan Tabungan (proyeksi, tidak diposting). */
    public static final BigDecimal SAVINGS_ANNUAL_RATE = new BigDecimal("0.03");

    /** Bunga tahunan Deposito (proyeksi, tidak diposting). */
    public static final BigDecimal DEPOSIT_ANNUAL_RATE = new BigDecimal("0.05");

    private BankingRules() {
        // kelas utilitas: tidak boleh dibuat instance
    }
}
