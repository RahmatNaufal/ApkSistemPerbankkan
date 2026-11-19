# Design System --- INT BANK

**Versi:** 1.1 (hasil review)\
**Acuan visual:** Gambar referensi pengguna (tidak disertakan dalam paket
dokumentasi). Dokumen ini dibuat **mandiri**: seluruh keputusan visual
ditulis eksplisit di bawah sehingga tidak bergantung pada gambar.\
**Arah desain:** Editorial, minimal, geometris, hangat, kontras tinggi,
dan ramah pengguna.

## 1. Prinsip Desain

INT BANK mengadaptasi bahasa visual dari referensi: - Kanvas utama putih
dengan latar luar beige. - Navigasi berbentuk kapsul memanjang. -
Panel/kartu dengan garis tepi hitam tipis dan sudut membulat. - Tata
letak editorial asimetris: kolom kartu di kiri dan headline utama di
kanan. - Tipografi besar dan tegas. - Aksen kuning lemon untuk
tombol/indikator penting. - Ornamen geometris sederhana sebagai
pengganti elemen foto musik pada referensi.

**Adaptasi untuk konteks bank:** jangan meniru konten musik. Ganti kartu
program/tim dengan ringkasan rekening, keamanan, aktivitas, dan layanan
finansial simulasi. Prioritaskan keterbacaan saldo, status, dan aksi
transaksi.

## 2. Palet Warna

  Token               Nilai       Penggunaan
  ------------------- ----------- -----------------------------------
  `--color-ink`       `#111111`   Teks utama, border, tombol utama
  `--color-paper`     `#FFFFFF`   Permukaan utama dan kartu
  `--color-sand`      `#E7E0D2`   Latar luar/section lembut
  `--color-cream`     `#F1EBDD`   Navigasi dan kartu sekunder
  `--color-accent`    `#F2F58A`   CTA, highlight, indikator pilihan
  `--color-muted`     `#6B685F`   Teks sekunder (5.6:1 di putih, 4.7:1 di cream; jangan di atas sand: 4.2:1)
  `--color-line`      `#171717`   Garis tepi dan divider
  `--color-success`   `#26734D`   Status sukses
  `--color-danger`    `#B43A32`   Status gagal/peringatan
  `--color-warning`   `#9A6500`   Status perlu perhatian

Warna status harus selalu disertai label teks atau ikon, bukan warna
saja.

### Catatan kontras (rumus WCAG 2.x, sudah dihitung)

| Pasangan | Rasio | Hasil |
|---|---|---|
| ink `#111` di accent `#F2F58A` | 16.4:1 | Lulus (pasangan utama CTA) |
| accent sebagai **teks** di putih | 1.15:1 | **Gagal**: accent hanya boleh sebagai latar |
| muted di sand | 4.24:1 | Gagal AA teks normal: pakai ink di sand |
| warning `#9A6500` di sand | 3.78:1 | Gagal: pakai di putih (4.96:1) atau gelapkan ke `#7A5000` |
| danger `#B43A32` di sand | 4.45:1 | Hampir: gunakan di putih (5.85:1) |
| success `#26734D` di putih / cream | 5.77 / 4.85 | Lulus |

Aturan: **teks berwarna status hanya di atas paper/cream.** Di atas sand
gunakan ink. Badge status memakai latar tint + teks ink + ikon.

## 3. Tipografi

-   **Heading:** sans-serif modern dengan karakter geometris; contoh:
    `Manrope`, `Plus Jakarta Sans`, atau fallback `Arial, sans-serif`.
-   **Body:** sans-serif yang sangat terbaca; gunakan keluarga yang sama
    untuk konsistensi.
-   **Angka saldo dan nominal:** gunakan tabular numerals agar angka
    rata dan mudah dibandingkan.
-   **Headline landing:** besar, rapat, dan pendek, berbahasa Indonesia.
-   Muat font dengan `font-display: swap` dan fallback; verifikasi
    `font-variant-numeric: tabular-nums` benar-benar berefek pada font
    terpilih (uji angka 1111 vs 8888 sejajar).
-   **Label form:** ukuran cukup besar dan kontras tinggi.

Skala ukuran desktop: - Display: 56--64 px, line-height 1.05. - H1:
40--48 px. - H2: 28--32 px. - H3: 20--24 px. - Body: 15--17 px. -
Caption/label: 12--14 px.

Skala mobile disesuaikan: display 36--42 px, H1 30--34 px, body minimum
14 px.

## 4. Layout dan Grid

### 4.1 Landing page

Mengikuti komposisi referensi: 1. Latar luar beige sebagai frame. 2.
Area konten putih di tengah. 3. Navbar kapsul di bagian atas. 4. Bagian
hero dua kolom: - Kiri: grid kartu statistik/layanan. - Kanan: headline,
deskripsi, CTA. 5. Bagian bawah hero: dua kartu metrik atau keunggulan.

Konten hero yang disarankan: - Headline: **"Perbankan jadi sederhana.
Uangmu, kendalimu."** - Deskripsi: "Kelola rekening, pantau transaksi,
dan lakukan transfer internal dengan pengalaman digital yang
sederhana." - CTA utama: "Mulai Sekarang" (ke Daftar; "Buka Dashboard" hanya jika
sudah login). - CTA sekunder: "Bedah OOP".

Kartu kiri berisi **contoh data fiktif berlabel "Contoh"**: - `Rekening Anda`
--- nomor tersamarkan dan jenis. - `Transfer cepat`. - `Perbankan aman`. -
`Aktivitas terbaru`. - `Tabungan`. Semua label berbahasa Indonesia.

Jangan menampilkan saldo pribadi pada landing publik. Informasi rekening
dan saldo hanya muncul setelah login.

### 4.2 Dashboard nasabah

-   Navbar/header kapsul dengan logo INT BANK, navigasi, avatar/profil,
    dan logout.
-   Salam singkat di area utama.
-   Kartu saldo utama yang paling menonjol.
-   Aksi cepat: Transfer, Setor, Tarik.
-   Grid dua kolom: aktivitas terbaru dan informasi rekening.
-   Riwayat lengkap berada pada halaman tersendiri.

### 4.3 Halaman transaksi

-   Gunakan panel putih dengan border hitam.
-   Judul dan deskripsi singkat.
-   Form satu kolom dengan label jelas.
-   Ringkasan transaksi sebelum konfirmasi.
-   Tombol utama hitam; tombol sekunder outline.
-   Pesan sukses/gagal muncul dekat hasil aksi dan tidak hanya berupa
    toast sementara.

### 4.3b Halaman baru (hasil review)

-   **Bedah OOP:** empat kartu pilar (ikon, definisi satu kalimat, cuplikan
    kode dari class sebenarnya), diagram `Account` → 3 subclass, dan
    **simulator**: pilih saldo dan nominal tarik, lalu tampilkan hasil
    ketiga jenis berdampingan (berhasil/ditolak + alasan, biaya, saldo
    akhir, proyeksi bunga) sehingga polimorfisme langsung terlihat.
-   **Buka rekening:** kartu radio per jenis dengan ringkasan aturan, tenor
    Deposito (3/6/12), dana awal, ringkasan, konfirmasi.
-   **Detail rekening:** kartu saldo, aturan jenis rekening, proyeksi bunga.
-   **Bukti transaksi:** layout cetak (print stylesheet), nomor referensi
    monospace, tombol Cetak.
-   **Error 403/404/500:** panel sederhana dengan tombol kembali.
-   **Admin audit log:** tabel padat dengan filter aktor/aksi/tanggal.

### 4.4 Admin

Gunakan gaya visual yang sama, tetapi tata letak lebih padat dan
berorientasi data: - Ringkasan statistik. - Tabel
nasabah/rekening/transaksi. - Filter dan pencarian. - Badge status. -
Aksi admin yang jelas dan membutuhkan konfirmasi.

## 5. Komponen UI

### Navbar

-   Bentuk pill/capsule.
-   Latar cream.
-   Border 1--1.5 px ink.
-   Logo di kiri.
-   Navigasi di tengah.
-   Login dan CTA di kanan.
-   Pada mobile, navigasi berubah menjadi menu ringkas.

### Buttons

-   **Primary:** latar ink, teks putih, radius pill.
-   **Secondary:** latar cream/white, border ink, teks ink.
-   **Accent:** latar lemon, teks ink; gunakan terbatas untuk CTA utama.
-   Tinggi minimum 44 px.
-   Sediakan hover, focus-visible, active, disabled, dan loading state.

### Cards

-   Latar putih atau cream.
-   Border 1--1.5 px hitam.
-   Radius 18--24 px.
-   Padding 20--28 px.
-   Hindari shadow tebal; bila perlu gunakan shadow sangat halus.
-   Kartu saldo dapat memakai latar ink dengan teks putih dan aksen
    lemon.

### Form fields

-   Label selalu terlihat.
-   Input tinggi 46--52 px.
-   Border ink tipis, radius 12--16 px.
-   Placeholder hanya sebagai contoh, bukan pengganti label.
-   Tampilkan helper text dan pesan validasi.
-   Nominal menggunakan format Rupiah yang konsisten.

### Tables

-   Header jelas dan kontras.
-   Baris diberi pemisah halus.
-   Pada mobile, gunakan horizontal scroll atau ubah menjadi kartu.
-   Sediakan empty state dan loading state.

### Status badge

-   `SUCCESS`: hijau + label "Berhasil".
-   `PENDING`: kuning/oranye + label "Diproses".
-   `FAILED`: merah + label "Gagal".
-   `ACTIVE`: hijau + label "Aktif".
-   `INACTIVE/BLOCKED`: merah/abu + label sesuai status.

### Notification / alert

-   Sukses: ikon centang + ringkasan.
-   Gagal: jelaskan cara memperbaiki input tanpa membocorkan detail
    teknis.
-   Konfirmasi transfer: tampilkan rekening tujuan tersamarkan, nama
    penerima (jika tersedia), nominal, dan biaya bila berlaku.

## 6. Spacing, Radius, dan Border

Spacing berbasis kelipatan 4: - `4`, `8`, `12`, `16`, `20`, `24`, `32`,
`40`, `48`, `64`, `80`.

Radius: - `--radius-sm: 10px` - `--radius-md: 16px` -
`--radius-lg: 22px` - `--radius-pill: 999px`

Border: - `1px solid var(--color-ink)` untuk mayoritas kartu/input. -
Garis divider 1px dengan opacity/warna muted. - Hindari dekorasi border
yang mengganggu keterbacaan.

## 7. Ikon dan Ornamen

-   Gunakan ikon garis sederhana dan konsisten (misalnya Lucide).
-   Ikon hanya memperkuat label; jangan menggantikan label pada aksi
    penting.
-   Motif geometris kecil (diamond/circle) boleh digunakan sebagai aksen
    visual seperti referensi.
-   Hindari foto stok yang tidak relevan.
-   Gunakan ilustrasi abstrak berbentuk geometri untuk kartu
    layanan/keamanan.

## 8. Responsif

Breakpoints acuan: - Mobile: `< 640px` - Tablet: `640–1023px` - Desktop:
`>= 1024px`

Aturan: - Desktop: hero dua kolom dan kartu asimetris. - Tablet: grid
dapat menjadi dua kolom dengan headline lebih kecil. - Mobile: satu
kolom; navbar menjadi menu ringkas; CTA dapat full-width. - Kartu tidak
boleh menyebabkan horizontal overflow. - Tabel panjang boleh memakai
scroll horizontal yang terkontrol. - Target sentuh minimal 44×44 px.

## 9. Aksesibilitas

-   Kontras teks memenuhi WCAG AA sejauh memungkinkan.
-   Semua input memiliki label yang terhubung.
-   Navigasi dapat digunakan dengan keyboard.
-   Focus indicator terlihat jelas.
-   Gunakan semantic HTML (`header`, `nav`, `main`, `section`, `form`,
    `button`).
-   Tambahkan `aria-label` untuk ikon tanpa teks.
-   Jangan menyampaikan status hanya melalui warna.
-   Hormati `prefers-reduced-motion`.

## 10. Motion dan Interaksi

-   Transisi ringan 150--220 ms.
-   Hover kartu hanya sedikit berubah (misalnya border/shadow).
-   Hindari animasi berlebihan pada dashboard keuangan.
-   Tombol submit menampilkan loading state dan mencegah klik berulang.
-   Aksi transfer menampilkan tahap review/konfirmasi sebelum dikirim.

## 11. Konten dan Bahasa

-   Bahasa utama: Bahasa Indonesia.
-   Nada: profesional, tenang, jelas, tidak berlebihan.
-   Gunakan "Saldo tersedia", "Transfer", "Setor dana", "Tarik dana",
    "Riwayat transaksi".
-   Hindari klaim seperti "100% aman" atau klaim layanan bank sungguhan.
-   Beri penanda bahwa ini adalah **simulasi perbankan untuk
    pembelajaran** pada footer atau halaman Tentang.

## 12. CSS Design Tokens

``` css
:root {
  --color-ink: #111111;
  --color-paper: #ffffff;
  --color-sand: #e7e0d2;
  --color-cream: #f1ebdd;
  --color-accent: #f2f58a;
  --color-muted: #6b685f;
  --color-line: #171717;
  --color-success: #26734d;
  --color-danger: #b43a32;
  --color-warning: #9a6500;      /* hanya di atas paper/cream */
  --color-focus: #1a4fd6;
  --color-success-bg: #e3f1e9;
  --color-danger-bg: #f7e1df;
  --color-warning-bg: #f8ebd0;

  --font-sans: "Manrope", "Plus Jakarta Sans", Arial, sans-serif;

  --radius-sm: 10px;
  --radius-md: 16px;
  --radius-lg: 22px;
  --radius-pill: 999px;

  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 20px;
  --space-6: 24px;
  --space-8: 32px;
  --space-10: 40px;
  --space-12: 48px;
  --space-16: 64px;

  --border-default: 1px solid var(--color-ink);
  --focus-ring: 3px solid var(--color-focus);
  --shadow-soft: 0 1px 2px rgba(17,17,17,.08);
}

.amount { font-variant-numeric: tabular-nums; }
:focus-visible { outline: var(--focus-ring); outline-offset: 2px; }
@media (prefers-reduced-motion: reduce) { * { transition: none !important; animation: none !important; } }
```

## 13. Do and Don't

### Do

-   Pertahankan frame beige, kanvas putih, navbar pill, kartu outline,
    dan aksen lemon dari referensi.
-   Buat saldo dan tombol transaksi mudah ditemukan.
-   Gunakan ruang kosong agar layout terasa lega.
-   Konsisten pada radius, border, dan tipografi.
-   Berikan konfirmasi sebelum transaksi dikirim.

### Don't

-   Jangan menyalin teks, logo, atau konten Musmentor dari gambar
    referensi.
-   Jangan menampilkan saldo atau data rekening pada halaman publik.
-   Jangan menggunakan warna aksen pada semua komponen.
-   Jangan menyembunyikan label form di placeholder.
-   Jangan membuat animasi yang mengganggu.
-   Jangan menjadikan tampilan sebagai pengganti validasi keamanan di
    backend.

## 14. Checklist Visual

-   [ ] Navbar kapsul dengan logo INT BANK.
-   [ ] Background luar beige dan konten utama putih.
-   [ ] Hero asimetris dua kolom pada desktop.
-   [ ] Kartu memiliki border hitam dan radius konsisten.
-   [ ] Aksen lemon digunakan secara selektif.
-   [ ] Headline besar dan deskripsi ringkas.
-   [ ] Dashboard menonjolkan saldo dan aksi cepat.
-   [ ] Form transaksi memiliki ringkasan konfirmasi.
-   [ ] Layout responsif pada ponsel.
-   [ ] Fokus keyboard dan kontras dapat digunakan.
-   [ ] Penanda simulasi akademik terlihat.
-   [ ] Tidak ada teks berwarna status di atas sand (cek tabel kontras).
-   [ ] Seluruh label UI berbahasa Indonesia.
-   [ ] Halaman Bedah OOP, Buka rekening, Bukti transaksi, dan Error tersedia.
