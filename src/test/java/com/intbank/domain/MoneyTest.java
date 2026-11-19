package com.intbank.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** Unit test untuk ENKAPSULASI nilai uang. */
class MoneyTest {

    @Test
    @DisplayName("Pecahan rupiah dibulatkan ke bawah")
    void pecahanDibulatkanKeBawah() {
        assertEquals(Money.of(123), Money.of(new BigDecimal("123.99")));
        assertEquals(Money.of(0), Money.of(new BigDecimal("0.90")));
    }

    @Test
    @DisplayName("Operasi plus/minus tidak mengubah objek asal (immutable)")
    void operasiTidakMengubahObjekAsal() {
        Money awal = Money.of(100_000);
        Money hasil = awal.plus(Money.of(50_000)).minus(Money.of(10_000));

        assertEquals(Money.of(140_000), hasil);
        assertEquals(Money.of(100_000), awal);
    }

    @Test
    @DisplayName("Equals berdasarkan nilai, bukan scale BigDecimal")
    void equalsBerdasarkanNilai() {
        assertEquals(Money.of(1_000), Money.of(new BigDecimal("1000.00")));
        assertEquals(Money.of(1_000).hashCode(), Money.of(new BigDecimal("1000.00")).hashCode());
        assertNotEquals(Money.of(1_000), Money.of(1_001));
    }

    @Test
    @DisplayName("Format tampilan Rupiah memakai titik sebagai pemisah ribuan")
    void formatRupiah() {
        assertEquals("Rp1.000.000", Money.of(1_000_000).toString());
        assertEquals("Rp0", Money.ZERO.toString());
    }

    @Test
    @DisplayName("times membulatkan hasil kali ke bawah")
    void timesMembulatkanKeBawah() {
        assertEquals(Money.of(3), Money.of(100).times(new BigDecimal("0.03")));
        assertEquals(Money.of(33), Money.of(1000).times(new BigDecimal("0.033")));
    }

    @Test
    @DisplayName("CompareTo dan penanda tanda uang bekerja")
    void compareToDanPenanda() {
        assertTrue(Money.of(200).compareTo(Money.of(100)) > 0);
        assertTrue(Money.ZERO.isZero());
        assertTrue(Money.of(1).isPositive());
        assertTrue(Money.of(-1).isNegative());
    }
}
