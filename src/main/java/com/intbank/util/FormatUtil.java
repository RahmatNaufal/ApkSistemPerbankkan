package com.intbank.util;

import com.intbank.domain.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Format tampilan dan parsing input Rupiah. Dipakai di lapisan aplikasi, bukan di domain. */
public final class FormatUtil {

    private static final DateTimeFormatter DATETIME =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", new Locale("id", "ID"));
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("id", "ID"));

    private FormatUtil() {
    }

    public static String rupiah(Money money) {
        if (money == null) {
            return "Rp0";
        }
        return "Rp" + String.format("%,d", money.toBigDecimal().longValue()).replace(',', '.');
    }

    /** Nomor rekening 10 digit ditampilkan hanya 4 digit terakhir. */
    public static String mask(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return "••••";
        }
        return "••••••" + accountNumber.substring(accountNumber.length() - 4);
    }

    public static String dateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DATETIME);
    }

    public static String date(LocalDate value) {
        return value == null ? "-" : value.format(DATE);
    }

    /** Menerima "1500000", "1.500.000", atau "Rp1.500.000". */
    public static Money parseAmount(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Nominal wajib diisi.");
        }
        String clean = input.replace("Rp", "").replace(".", "").replace(" ", "").replace(",", "").trim();
        if (!clean.matches("\\d+")) {
            throw new IllegalArgumentException("Nominal harus berupa angka bulat.");
        }
        return Money.of(new BigDecimal(clean));
    }

    public static String reference() {
        return "INV" + java.time.Instant.now().toEpochMilli()
                + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
