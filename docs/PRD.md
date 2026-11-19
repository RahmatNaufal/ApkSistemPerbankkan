# Product Requirements Document (PRD) --- INT BANK

**Versi:** 1.1 (hasil review)\
**Status:** Draft untuk pengembangan akademik\
**Nama produk:** INT BANK\
**Jenis produk:** Website simulasi perbankan digital

## 1. Ringkasan Produk

INT BANK adalah website simulasi layanan perbankan digital untuk nasabah
dan administrator. Sistem menyediakan autentikasi, dashboard, informasi
rekening, setor dana, tarik dana, transfer antar rekening internal,
riwayat transaksi, pengelolaan profil, serta pengelolaan nasabah oleh
admin.

Proyek ini memiliki tujuan pembelajaran khusus: menerapkan empat pilar
Object-Oriented Programming (OOP) secara nyata dalam logika bisnis,
bukan sekadar menampilkan contoh class.

> **Batasan penting:** INT BANK adalah simulasi akademik. Sistem tidak
> terhubung ke bank, jaringan pembayaran, ATM, atau rekening sungguhan.
> Jangan gunakan uang atau data pribadi nyata.

## 2. Tujuan dan Ukuran Keberhasilan

### Tujuan

-   Menyediakan pengalaman website perbankan yang konsisten dan mudah
    digunakan.
-   Memastikan proses transaksi memiliki validasi dan pencatatan yang
    benar.
-   Menunjukkan penerapan enkapsulasi, abstraksi, pewarisan, dan
    polimorfisme.
-   Memisahkan antarmuka, logika bisnis, dan akses database agar mudah
    diuji dan dikembangkan.

### Kriteria keberhasilan

-   Nasabah dapat login dan hanya melihat rekening miliknya.
-   Setor, tarik, dan transfer menolak nominal tidak valid atau saldo
    tidak cukup.
-   Transfer memperbarui saldo pengirim dan penerima secara atomik.
-   Setiap transaksi sukses tercatat dan dapat dilihat di riwayat.
-   Admin dapat melihat dan mengelola status nasabah sesuai hak akses.
-   Seluruh empat pilar OOP dapat ditunjukkan melalui class dan alur
    bisnis yang jelas. **Terukur:** setiap pilar punya minimal 1 kelas
    sumber, 1 unit test, dan 1 layar/halaman yang memperlihatkannya
    (lihat matriks keterlacakan di `architecture.md` §5.6 dan halaman
    "Bedah OOP" di §4.1 butir 14).
-   Unit test domain lulus 100%, termasuk uji konkurensi dua penarikan
    bersamaan yang tidak membuat saldo negatif.
-   Antarmuka responsif pada desktop, tablet, dan ponsel.

## 3. Pengguna dan Hak Akses

### 3.1 Nasabah

Nasabah dapat: - Mendaftar dan login. - Melihat dashboard dan rekening
miliknya. - Melihat saldo dan detail rekening. - Melakukan setor dana
simulasi. - Melakukan tarik dana. - Transfer ke rekening INT BANK lain
yang aktif. - Melihat, mencari, dan memfilter riwayat transaksi. -
Mengubah informasi profil yang diizinkan. - Logout.

Nasabah tidak dapat: - Mengakses data nasabah lain. - Mengubah saldo
secara langsung. - Mengubah status rekening atau transaksi. - Mengakses
halaman admin.

### 3.2 Admin

Admin dapat: - Login melalui akun dengan role ADMIN. - Melihat ringkasan
sistem. - Melihat daftar nasabah dan rekening. - Mencari nasabah
berdasarkan nama atau nomor rekening. - Mengaktifkan atau menonaktifkan
nasabah/rekening sesuai aturan. - Melihat transaksi untuk keperluan
pemantauan. - Melihat ringkasan dan laporan transaksi.

Admin tidak boleh mengubah saldo dengan mengedit angka secara langsung.
Koreksi saldo, jika kelak diperlukan, harus berupa transaksi penyesuaian
khusus dengan alasan, otorisasi, dan audit trail. Fitur ini berada di
luar MVP.

## 4. Ruang Lingkup Fitur

### 4.1 MVP --- wajib dibuat

1.  **Landing page:** identitas INT BANK, navigasi, pengenalan layanan,
    CTA login/daftar.
2.  **Register:** nama, email, nomor telepon, password; validasi dan
    pemeriksaan email unik.
3.  **Login/logout:** autentikasi dan pembatasan akses berdasarkan role.
4.  **Dashboard nasabah:** saldo, nomor rekening tersamarkan, ringkasan
    transaksi, tombol aksi cepat.
5.  **Rekening:** detail rekening, jenis, status, dan saldo.
6.  **Setor dana simulasi:** menambah saldo melalui form dengan
    validasi.
7.  **Tarik dana:** mengurangi saldo dengan validasi saldo minimum dan
    batas yang ditentukan.
8.  **Transfer internal:** transfer ke rekening INT BANK lain.
9.  **Riwayat transaksi:** daftar transaksi dengan jenis, tanggal,
    nominal, status, dan pencarian/filter.
10. **Profil:** melihat dan memperbarui data profil yang diizinkan.
11. **Admin dashboard:** ringkasan nasabah, rekening, dan transaksi.
12. **Admin nasabah:** daftar, pencarian, detail, dan perubahan status.
13. **Notifikasi UI:** pesan berhasil/gagal yang jelas dan tidak
    membocorkan detail sensitif.
14. **Bedah OOP (halaman publik, wajib karena ini proyek OOP):** satu
    halaman yang menjelaskan 4 pilar dan memuat simulator: pengguna
    memilih jenis rekening (Tabungan/Giro/Deposito) + nominal, lalu
    melihat hasil `withdraw()` dan proyeksi bunga yang berbeda untuk
    tiap jenis. Hanya membaca data demo; tidak menyentuh saldo asli.
15. **Buka rekening baru:** nasabah dapat membuka rekening Tabungan atau
    Giro tambahan, dan Deposito (nominal awal dipindahkan dari
    rekening miliknya, pilih tenor 3/6/12 bulan). Tanpa fitur ini
    ketiga jenis rekening tidak dapat didemonstrasikan.
16. **Detail rekening & bukti transaksi:** halaman detail per rekening
    dan halaman bukti (receipt) yang dapat dicetak.
17. **Halaman error:** 403, 404, dan 500 dengan pesan aman.

### 4.2 Di luar MVP

-   Integrasi bank atau payment gateway sungguhan.
-   Kartu debit/kredit, QRIS, pembayaran tagihan, pinjaman, investasi.
-   OTP melalui SMS/email dan biometrik.
-   Multi-currency.
-   Transfer antarbank.
-   Bunga atau pajak berdasarkan regulasi nyata.
-   Aplikasi mobile native.
-   Koreksi saldo manual oleh admin.

## 5. Alur Utama Pengguna

### 5.1 Registrasi dan pembukaan rekening

1.  Pengguna mengisi formulir pendaftaran.
2.  Server memvalidasi format dan keunikan email.
3.  Password di-hash sebelum disimpan.
4.  Sistem membuat user (role **selalu** CUSTOMER, tidak pernah dari
    input form), data nasabah, dan satu rekening **Tabungan** dengan
    saldo awal Rp0 dalam satu transaksi database. Nomor rekening
    10 digit dibuat server (unik, bukan urutan yang mudah ditebak).
5.  Sistem menampilkan konfirmasi dan mengarahkan pengguna ke halaman
    login (keputusan final; tidak auto-login).

### 5.2 Setor dana simulasi

1.  Nasabah membuka menu Setor dan **memilih rekening tujuan** (jika
    punya lebih dari satu).
2.  Mengisi nominal positif.
3.  Server memeriksa autentikasi, kepemilikan rekening, status rekening,
    dan batas nominal.
4.  Sistem menambah saldo dan membuat catatan transaksi dalam satu
    transaksi database.
5.  Sistem menampilkan saldo terbaru dan nomor referensi transaksi.

### 5.3 Tarik dana

1.  Nasabah memilih rekening sumber dan memasukkan nominal penarikan.
2.  Server memvalidasi nominal, status rekening, saldo tersedia, saldo
    minimum, dan batas penarikan.
3.  Jika valid, sistem mengurangi saldo dan mencatat transaksi secara
    atomik.
4.  Jika gagal, saldo tidak berubah dan alasan kegagalan ditampilkan
    secara aman.

### 5.4 Transfer internal

1.  Nasabah memilih rekening sumber, memasukkan nomor rekening tujuan
    dan nominal. Form membawa `idempotency key` acak (hidden field).
2.  Sistem mencari rekening tujuan.
3.  Sistem memvalidasi rekening tujuan aktif, bukan rekening sendiri,
    dan nominal valid.
4.  Sistem memeriksa saldo tersedia pengirim.
5.  Sistem mengurangi saldo pengirim, menambah saldo penerima, serta
    membuat catatan debit dan kredit yang saling terhubung dalam satu
    transaksi database.
6.  Jika salah satu langkah gagal, seluruh perubahan dibatalkan.
7.  Sistem menampilkan bukti transaksi dan referensi unik.

### 5.5 Riwayat transaksi

Nasabah hanya melihat transaksi yang berkaitan dengan rekening miliknya.
Filter minimal: rentang tanggal, jenis transaksi, dan status. Urutan
default terbaru di atas.

## 6. Aturan Bisnis dan Validasi

-   Semua nominal uang menggunakan `BigDecimal` di Java dan `DECIMAL` di
    database, bukan `double` atau `float`.
-   Mata uang MVP adalah Rupiah (IDR), tanpa pecahan desimal pada input
    pengguna.
-   Nominal harus lebih besar dari nol dan memenuhi batas transaksi yang
    dikonfigurasi.
-   Saldo tidak boleh negatif.
-   Rekening harus berstatus ACTIVE untuk bertransaksi.
-   Nasabah hanya boleh melakukan transaksi dari rekening yang
    dimilikinya.
-   Transfer tidak boleh ke rekening yang sama.
-   Rekening tujuan harus ditemukan dan ACTIVE.
-   Transfer harus atomik: debit, kredit, dan pencatatan transaksi
    berhasil bersama-sama atau gagal bersama-sama.
-   Permintaan transaksi yang sama tidak boleh diproses dua kali;
    gunakan reference/idempotency key untuk mencegah pengiriman ulang.
-   Saldo dan catatan transaksi tidak boleh diubah dari frontend.
-   Waktu transaksi disimpan konsisten (misalnya UTC) dan ditampilkan
    sesuai zona waktu aplikasi.
-   Saldo yang ditampilkan berasal dari server/database, bukan nilai
    yang dipercaya dari browser.
-   Batas saldo minimum, batas setor/tarik/transfer, dan biaya (jika
    digunakan) harus menjadi konfigurasi bisnis, bukan angka tersebar di
    kode.

### Aturan per jenis rekening (keputusan final, nilai berasal dari konfigurasi)

| Aturan | Tabungan | Giro | Deposito |
|---|---|---|---|
| Saldo minimum | Rp50.000 | Rp0 | - |
| Biaya tarik / transfer keluar | Rp0 | Rp2.500 | - |
| Setor tambahan | Ya | Ya | **Tidak** (hanya dana awal) |
| Tarik | Ya | Ya | Hanya **penuh** setelah jatuh tempo |
| Bunga | 3% p.a. | 0% | 5% p.a. |

Bunga pada MVP hanya **proyeksi yang ditampilkan** (`interestProjection()`),
tidak diposting ke saldo. Ini menyelesaikan kontradiksi antara
`calculateInterest()` di arsitektur dan "bunga" yang berada di luar MVP.
Biaya Giro diambil dari saldo pengirim sebagai bagian dari debit yang sama;
saldo penerima bertambah sebesar nominal transfer (tanpa biaya).

### Transisi status rekening

- `ACTIVE` ⇄ `INACTIVE` (admin atau nasabah tidak dapat mengubah sendiri; admin saja).
- `ACTIVE` / `INACTIVE` → `BLOCKED` (admin, wajib alasan, tercatat di audit log).
- `BLOCKED` → `ACTIVE` (admin, tercatat).
- Apa pun → `CLOSED` hanya jika saldo = 0. `CLOSED` bersifat final.
- Menonaktifkan **nasabah** = `users.enabled = false`: tidak dapat login,
  semua rekening miliknya tidak dapat bertransaksi.

### Perilaku transaksi gagal (keputusan final)

Karena perubahan saldo di-rollback saat gagal, **MVP hanya menyimpan
transaksi berstatus SUCCESS di tabel `transactions`**. Kegagalan dicatat
ke log aplikasi/audit (tanpa data sensitif), bukan ke ledger. Status
`PENDING`, `FAILED`, `REVERSED` disiapkan di enum namun belum dipakai di MVP.

### Status yang disarankan

-   Rekening: `ACTIVE`, `INACTIVE`, `BLOCKED`, `CLOSED`.
-   Transaksi: `PENDING`, `SUCCESS`, `FAILED`, `REVERSED` (MVP dapat
    menggunakan SUCCESS/FAILED bila proses sinkron).
-   Jenis transaksi: `DEPOSIT`, `WITHDRAWAL`, `TRANSFER_IN`,
    `TRANSFER_OUT`, `DISBURSEMENT` (pencairan Deposito). Pembukaan Deposito
    dicatat sebagai `TRANSFER_OUT` pada rekening sumber dan `DEPOSIT`
    pada Deposito baru.

## 7. Kebutuhan Nonfungsional

### Keamanan

-   Gunakan Spring Security untuk autentikasi dan otorisasi.
-   Password disimpan menggunakan algoritma hash adaptif seperti
    BCrypt/Argon2 melalui framework; jangan simpan plaintext.
-   Validasi dilakukan di server meskipun validasi frontend tersedia.
-   Lindungi endpoint berdasarkan role dan kepemilikan data.
-   Gunakan proteksi CSRF jika memakai session/cookie authentication.
-   Batasi percobaan login: 5 kali gagal berturut-turut mengunci akun 15
    menit (kolom `failed_login_attempts`, `locked_until`); pesan gagal
    login selalu sama sehingga tidak mengungkap apakah email terdaftar.
-   Kebijakan password: minimal 8 karakter, kombinasi huruf dan angka.
    Cookie sesi `HttpOnly`, `Secure`, `SameSite=Lax`; sesi diganti baru
    saat login (mencegah session fixation).
-   Akun ADMIN dibuat lewat seed/migrasi, tidak lewat form pendaftaran.
-   Identifikasi rekening di URL memakai nomor rekening, dan setiap
    permintaan tetap diperiksa kepemilikannya (anti-IDOR).
-   Jangan menulis password, token, atau data sensitif ke log.
-   Gunakan HTTPS saat deployment.
-   Simpan secret di environment variable, bukan repository.
-   Catat aktivitas penting admin dalam audit log.

### Kinerja dan ukuran data (target demo)

-   Halaman utama dimuat < 2 detik pada data demo (1.000 nasabah, 50.000 transaksi).
-   Riwayat transaksi selalu berpaginasi (20 baris per halaman) dan
    diindeks pada `(account_id, created_at)`.

### Keandalan dan integritas

-   Gunakan transaksi database ACID untuk perubahan saldo.
-   Tangani konkurensi saat dua transaksi mengakses rekening yang sama,
    misalnya dengan row locking atau optimistic locking dan retry
    terkontrol.
-   Buat nomor referensi transaksi unik.
-   Kegagalan harus menghasilkan pesan yang jelas tanpa mengubah saldo
    sebagian.

### Kegunaan dan aksesibilitas

-   Bahasa antarmuka utama: Bahasa Indonesia.
-   Layout responsif.
-   Form memiliki label, validasi, dan pesan error yang mudah dipahami.
-   Status tidak hanya dibedakan dengan warna; sertakan teks atau ikon.
-   Konfirmasi sebelum tindakan yang berdampak pada saldo.

## 8. Kebutuhan Halaman

  Halaman           Konten utama                                Akses
  ----------------- ------------------------------------------- ---------
  Landing           Hero, ringkasan layanan, CTA                Publik
  Login             Email dan password                          Publik
  Register          Form pendaftaran                            Publik
  Dashboard         Saldo, rekening, aktivitas, quick actions   Nasabah
  Transfer          Tujuan, nominal, konfirmasi                 Nasabah
  Setor             Nominal dan konfirmasi                      Nasabah
  Tarik             Nominal dan konfirmasi                      Nasabah
  Riwayat           Tabel, filter, pencarian                    Nasabah
  Profil            Data dan pengaturan akun                    Nasabah
  Admin dashboard   Statistik dan aktivitas                     Admin
  Admin nasabah     Daftar, pencarian, status                   Admin
  Admin transaksi   Daftar dan filter transaksi                 Admin
  Admin audit log   Aktivitas admin & kejadian keamanan         Admin
  Admin detail      Detail nasabah, rekening, ubah status       Admin
  Buka rekening     Pilih jenis, tenor deposito, dana awal      Nasabah
  Detail rekening   Info, aturan jenis rekening, proyeksi bunga Nasabah
  Bukti transaksi   Receipt dengan nomor referensi              Nasabah
  Bedah OOP         Penjelasan 4 pilar + simulator              Publik
  Error 403/404/500 Pesan aman, tautan kembali                  Publik

## 9. Data Utama

-   **User:** id, email, password_hash, role, enabled, failed_login_attempts, locked_until, created_at.
-   **Customer:** id, user_id, full_name, phone, address (opsional),
    created_at.
-   **Account:** id, account_number, customer_id, account_type, balance,
    status, created_at, version; khusus Deposito: tenor_months, maturity_date.
-   **Transaction:** id, reference_number, account_id, type, amount, fee,
    balance_after, status, description, created_at, created_by.
-   **Transfer:** id, transfer_reference, transaction_out_id, transaction_in_id,
    source_account_id, destination_account_id, amount, created_at.
-   **IdempotencyKey:** id, user_id, key, request_hash, response_ref, created_at; unik pada (user_id, key).
-   **AuditLog:** id, actor_user_id, action, target_type, target_id,
    reason, created_at, metadata yang tidak sensitif.

Relasi dan constraint final ditentukan pada desain database. Nomor
rekening dan reference number harus unik. Email harus unik. Foreign key
wajib digunakan.

## 10. Kriteria Penerimaan

-   Register menolak email duplikat dan input tidak valid.
-   Login berhasil untuk kredensial benar dan ditolak untuk kredensial
    salah.
-   Nasabah tidak dapat mengakses URL admin.
-   Nasabah A tidak dapat melihat rekening atau transaksi nasabah B
    dengan mengganti ID/URL.
-   Setor dengan nominal nol/negatif ditolak.
-   Tarik melebihi saldo tersedia ditolak tanpa mengubah saldo.
-   Transfer ke rekening tidak ditemukan, nonaktif, atau rekening
    sendiri ditolak.
-   Transfer dengan saldo cukup mengurangi saldo pengirim dan menambah
    saldo penerima dengan jumlah yang sama (dikurangi biaya bila biaya
    memang diaktifkan).
-   Kegagalan saat proses transfer membatalkan seluruh perubahan.
-   Pengiriman ulang request tidak menggandakan transaksi.
-   Riwayat menampilkan transaksi sesuai rekening yang dimiliki
    pengguna.
-   Admin dapat mengubah status rekening sesuai izin dan perubahan
    tercatat.
-   Tampilan dapat digunakan pada lebar desktop dan mobile.

## 11. Asumsi dan Keputusan

Keputusan yang sebelumnya terbuka sudah diputuskan di §6 (jenis rekening,
biaya, bunga, deposito, transaksi gagal). Yang masih perlu dikonfirmasi
dengan dosen/pembimbing hanyalah **stack**: jika mata kuliah mewajibkan
bahasa selain Java, domain model di `domain-reference/` dapat diporting
satu-satu karena hanya memakai fitur OOP umum.

-   Backend: Java Spring Boot.
-   Frontend: HTML, CSS, dan JavaScript; template server-side Thymeleaf
    direkomendasikan agar implementasi awal lebih sederhana.
-   Database: MySQL.
-   Jenis rekening: Tabungan, Giro, Deposito (aturan di §6).
-   Setor dana adalah simulasi, bukan integrasi uang sungguhan.
-   Nilai batas transaksi dan saldo minimum ditetapkan sebagai
    konfigurasi demo.
