# Review Rancangan INT BANK

Dokumentasi asli sudah kuat di keamanan dan integritas transaksi. Kekurangan
utamanya ada pada **tujuan inti proyek (4 pilar OOP)**, **inkonsistensi data
antar dokumen**, dan **keputusan yang masih menggantung**.

## A. Kritis (akan menghambat implementasi atau nilai)

| # | Kekurangan | Perbaikan (file) |
|---|---|---|
| 1 | Ketiga jenis rekening tidak bisa didemonstrasikan: registrasi hanya membuat satu rekening dan tidak ada fitur "buka rekening". | Fitur Buka rekening + Deposito (PRD §4.1 #15) |
| 2 | Tidak ada tempat menunjukkan 4 pilar ke dosen/penguji. | Halaman Bedah OOP + matriks keterlacakan (PRD #14, arch §5.6) |
| 3 | Skema DB tidak konsisten: PRD `created_by`, arsitektur `account_id`; tabel `transfers` berbeda; `reference_number` UNIQUE tidak bisa dipakai bersama dua baris transfer. | Skema tunggal (arch §8) |
| 4 | Idempotency key disebut berkali-kali tetapi tidak punya tempat penyimpanan. | Tabel `idempotency_keys` (arch §7.3, §8) |
| 5 | Kontradiksi: "saldo tidak boleh negatif" vs. Giro; "bunga di luar MVP" vs. `calculateInterest()`; status FAILED vs. rollback (baris gagal ikut hilang). | Aturan per jenis, bunga sebagai proyeksi, hanya SUCCESS disimpan (PRD §6) |
| 6 | Kolom Deposito (tenor, jatuh tempo) tidak ada di DB; strategi pewarisan JPA tidak ditentukan. | `SINGLE_TABLE` + kolom nullable + CHECK (arch §5.8, §8) |

## B. Penting

7. **Liskov:** Deposito menolak tarik tanpa kontrak yang tertulis. Tulis kontrak "boleh ditolak" di base class dan sediakan `canWithdraw()` (arch §5.3).
8. `Transaction` "abstract/strategy sesuai kebutuhan" terlalu longgar. Putuskan: satu kelas + enum, variasi lewat `TransactionPolicy` (§5.5).
9. Tidak ada `Money` value object; `BigDecimal` mengalir di semua layer (§5.1).
10. Urutan penguncian "konsisten" tidak didefinisikan. Putuskan: `id` menaik + pessimistic lock + `@Version` (§7.3).
11. Nama `AccessDeniedException` bentrok dengan Spring Security → `AccountAccessDeniedException`.
12. Batas login, kebijakan password, flag cookie, pembuatan akun admin, anti-IDOR tidak punya spesifikasi (PRD §7).
13. Transisi status rekening tidak ada ("sesuai aturan" tanpa aturan) (PRD §6).
14. Tidak ada `balance_after` di ledger, CHECK constraint, indeks riwayat, paginasi.
15. Uji konkurensi dengan H2 menyesatkan; gunakan MySQL via Testcontainers. Waktu perlu `Clock` agar jatuh tempo bisa diuji.
16. Halaman kurang: detail rekening, bukti transaksi, 403/404/500, audit log admin, detail nasabah admin.

## C. Design system

17. Bergantung pada gambar referensi yang tidak ada di paket. Dibuat mandiri.
18. Kontras: accent `#F2F58A` sebagai teks 1.15:1; warning di sand 3.78:1; muted di sand 4.24:1 (tabel di designSystem §2).
19. Campuran bahasa: headline dan label kartu berbahasa Inggris padahal UI wajib Indonesia.
20. Token belum lengkap: focus ring, latar status, ukuran font, `reduced-motion`.

## D. Yang sudah bagus (dipertahankan)

Pemisahan layer dan tanggung jawab komponen, aturan "controller tidak mengubah
saldo", BigDecimal/DECIMAL, validasi server, batas MVP yang jelas, kriteria
penerimaan yang dapat diuji, dan penanda simulasi akademik.

## Bukti

`domain-reference/` berisi implementasi Java murni (tanpa library) dari domain
yang diperbaiki. Jalankan:

    cd domain-reference
    javac -d out src/com/intbank/domain/*.java DomainDemo.java
    java -cp out DomainDemo        # 22 pemeriksaan harus PASS

Catatan: yang diuji hanya logika domain. Locking/transaksi MySQL, Spring
Security, dan UI belum dibangun maupun diuji.
