# Architecture --- INT BANK

**Versi:** 1.1 (hasil review)\
**Status:** Draft arsitektur aplikasi akademik

## 1. Tujuan Arsitektur

Arsitektur INT BANK memisahkan presentasi, logika bisnis, domain OOP,
dan akses data. Fokus utama adalah menjaga integritas saldo, kontrol
akses, kemudahan pengujian, dan keterlihatan penerapan empat pilar OOP.

## 2. Stack Teknologi

  Lapisan             Teknologi
  ------------------- ---------------------------------------
  Bahasa backend      Java (versi LTS yang didukung proyek)
  Framework backend   Spring Boot
  Web/security        Spring MVC, Spring Security
  Template UI         Thymeleaf
  Frontend            HTML, CSS, JavaScript
  Database            MySQL
  ORM                 Spring Data JPA / Hibernate
  Build               Maven
  Pengujian           JUnit 5, Mockito, Spring Boot Test
  Version control     Git

Untuk tahap awal, gunakan satu aplikasi Spring Boot yang menyajikan
halaman Thymeleaf dan endpoint controller. Pemisahan microservices tidak
diperlukan untuk skala tugas akademik ini.

## 3. Gambaran Arsitektur

``` text
Browser (Nasabah / Admin)
        |
        | HTTPS
        v
Spring MVC Controller
        |
        +---- Spring Security (Authentication & Authorization)
        |
        v
Application / Service Layer
        |
        +---- AuthService
        +---- AccountService
        +---- TransactionService
        +---- TransferService
        +---- AdminService
        |
        v
Domain Model (OOP)
        |
        +---- Customer
        +---- Account (abstract)
        |       +---- SavingsAccount
        |       +---- CurrentAccount
        |       +---- DepositAccount
        +---- Transaction (abstract/strategy sesuai kebutuhan)
        |
        v
Repository Layer (Spring Data JPA)
        |
        v
MySQL Database
```

Controller tidak boleh mengubah saldo secara langsung. Controller
menerima request, memvalidasi format input, memanggil service, lalu
mengembalikan view/response. Semua aturan bisnis berada pada
service/domain.

## 4. Struktur Folder yang Disarankan

``` text
int-bank/
├── pom.xml
├── README.md
├── docs/
│   ├── PRD.md
│   ├── architecture.md
│   └── designSystem.md
└── src/
    ├── main/
    │   ├── java/com/intbank/
    │   │   ├── IntBankApplication.java
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java
    │   │   │   └── WebConfig.java
    │   │   ├── controller/
    │   │   │   ├── AuthController.java
    │   │   │   ├── DashboardController.java
    │   │   │   ├── AccountController.java
    │   │   │   ├── TransactionController.java
    │   │   │   └── AdminController.java
    │   │   ├── dto/
    │   │   │   ├── RegisterRequest.java
    │   │   │   ├── TransferRequest.java
    │   │   │   └── TransactionResponse.java
    │   │   ├── domain/
    │   │   │   ├── User.java
    │   │   │   ├── Customer.java
    │   │   │   ├── Account.java
    │   │   │   ├── SavingsAccount.java
    │   │   │   ├── CurrentAccount.java
    │   │   │   ├── DepositAccount.java
    │   │   │   ├── Money.java
    │   │   │   ├── AccountStatus.java
    │   │   │   ├── Transaction.java
    │   │   │   └── Transfer.java
    │   │   ├── service/
    │   │   │   ├── AuthService.java
    │   │   │   ├── AccountService.java
    │   │   │   ├── TransactionService.java
    │   │   │   ├── TransferService.java
    │   │   │   └── AdminService.java
    │   │   ├── repository/
    │   │   ├── config/BankingProperties.java   (@ConfigurationProperties: limit, biaya, saldo minimum)
    │   │   ├── exception/
    │   │   └── security/
    │   └── resources/
    │       ├── templates/
    │       │   ├── auth/
    │       │   ├── customer/
    │       │   ├── admin/
    │       │   └── fragments/
    │       ├── static/
    │       │   ├── css/
    │       │   ├── js/
    │       │   └── images/
    │       └── application.properties
    └── test/
        └── java/com/intbank/
```

Nama package dan folder dapat disesuaikan, tetapi pemisahan tanggung
jawab harus dipertahankan.

## 5. Domain Model dan Empat Pilar OOP

Implementasi acuan yang sudah dikompilasi dan diuji (22 pemeriksaan) ada di
`domain-reference/`. Salin ke `src/main/java/com/intbank/domain/`, lalu tambahkan
anotasi JPA. Perilaku bisnis tetap di class domain (rich domain model),
bukan di service.

### 5.1 Enkapsulasi
-   Field `private`, **tanpa setter publik** untuk saldo dan status.
-   Saldo hanya berubah lewat `credit()` dan `withdraw()` yang memvalidasi
    nominal, status, dan aturan jenis rekening.
-   Uang dibungkus **value object `Money`** (immutable, IDR tanpa pecahan,
    berbasis `BigDecimal`). Tidak ada `BigDecimal`/`long` telanjang yang
    mengalir antar layer.
-   Perubahan status lewat `activate()`, `deactivate()`, `block()`, `close()`
    yang menjaga aturan transisi (mis. `close()` hanya jika saldo nol).
-   DTO untuk data masuk/keluar; entity tidak dikirim ke view.
-   JPA butuh konstruktor tanpa argumen: buat `protected Account() {}`
    supaya tidak bisa dipakai kode lain; gunakan *field access*
    (`@Id` pada field) agar Hibernate tidak memerlukan setter.

### 5.2 Abstraksi
`Account` (abstract) mendefinisikan **kontrak** dan **urutan langkah**
(*template method*): `credit()` dan `withdraw()` bersifat `final`, memanggil
validasi umum, lalu hook yang diimplementasikan subclass:

| Hook | Wajib? | Fungsi |
|---|---|---|
| `typeName()` | abstract | Nama jenis untuk UI |
| `minimumBalance()` | abstract | Saldo minimum |
| `withdrawalFee(amount)` | abstract | Biaya tarik/transfer keluar |
| `projectedInterest(months)` | abstract | Proyeksi bunga (tidak diposting) |
| `ensureCanWithdraw(...)` | opsional | Aturan tarik tambahan |
| `ensureCanCredit(...)` | opsional | Aturan setor tambahan |

Dengan begini aturan yang sama tidak ditulis tiga kali, dan subclass tidak
dapat melewati validasi dasar.

### 5.3 Pewarisan
```text
             Account (abstract)
              /      |       \
   SavingsAccount CurrentAccount DepositAccount
```
Tabel aturan ada di PRD §6. Hierarki sengaja dangkal (1 tingkat).
Tiga subclass memang *is-a* rekening dan berbeda perilaku, bukan sekadar
berbeda data. Untuk variasi kecil (biaya, batas) gunakan komposisi
(`FeePolicy`/`LimitPolicy` dari konfigurasi), bukan subclass baru.

**Catatan Liskov (LSP).** Deposito menolak `withdraw()` sebelum jatuh tempo.
Ini tidak melanggar LSP **selama kontrak di base class menyatakan bahwa
penarikan dapat ditolak sesuai aturan jenis rekening** (sudah tertulis pada
Javadoc `Account`). Pelanggaran LSP baru terjadi bila subclass melempar
`UnsupportedOperationException` untuk method yang dikontrakkan selalu tersedia.
Sediakan `canWithdraw(amount, today)` supaya UI menonaktifkan tombol
tanpa memakai `instanceof`.

### 5.4 Polimorfisme
Service dan `TransferService` hanya bergantung pada tipe `Account`:
```java
Account from = accounts.findForUpdate(sourceId);   // bisa Savings/Current/Deposit
Account to   = accounts.findForUpdate(destId);
from.validateWithdraw(amount, today);   // aturan berbeda per jenis
to.validateCredit(amount);
Money fee = from.withdraw(amount, today);
to.credit(amount);
```
Perilaku yang tampak berbeda untuk panggilan yang sama: saldo minimum,
biaya Giro Rp2.500, penolakan setor/tarik Deposito, dan
`interestProjection()`. Karena logika ini mengubah hasil transaksi nyata
(bukan sekadar mencetak teks), polimorfisme terlihat di alur bisnis.
Halaman "Bedah OOP" memvisualisasikannya.

### 5.5 Kebijakan transaksi (opsional)
`Transaction` tetap **satu kelas konkret** dengan enum `TransactionType`.
Jangan membuat subclass `Deposit/Withdrawal/Transfer`: perbedaannya hanya
data, bukan perilaku. Variasi perilaku (biaya, batas harian) dimodelkan
sebagai interface `TransactionPolicy` yang diinjeksi dari konfigurasi.

### 5.6 Matriks keterlacakan pilar OOP (bahan laporan/presentasi)

| Pilar | Kelas sumber | Unit test | Tampil di UI |
|---|---|---|---|
| Enkapsulasi | `Money`, `Account` (private balance, tanpa setter) | credit 0/negatif ditolak; saldo tidak berubah | Pesan validasi form |
| Abstraksi | `Account` (abstract + template method + hook) | `canWithdraw` tidak mengubah state | Bedah OOP: diagram kelas |
| Pewarisan | `SavingsAccount`, `CurrentAccount`, `DepositAccount` | aturan tiap jenis | Detail rekening: aturan jenis |
| Polimorfisme | `Account.withdraw()` dipanggil via tipe dasar; `interestProjection()` | biaya Giro, minimum Tabungan, Deposito | Simulator Bedah OOP |

### 5.7 Injeksi waktu
Jangan memanggil `LocalDate.now()` di domain. Teruskan `today` (atau
`java.time.Clock`) dari service agar jatuh tempo Deposito dapat diuji
tanpa menunggu berbulan-bulan.

### 5.8 Pemetaan JPA untuk pewarisan
Gunakan `@Inheritance(strategy = SINGLE_TABLE)` dengan
`@DiscriminatorColumn(name = "account_type")` dan nilai `SAVINGS`,
`CURRENT`, `DEPOSIT`. Kolom khusus Deposito (`tenor_months`,
`maturity_date`) nullable pada tabel yang sama. Keuntungan: query
polimorfik `findById` mengembalikan subclass yang benar tanpa join.

## 6. Tanggung Jawab Komponen

  -----------------------------------------------------------------------
  Komponen                Tanggung jawab          Tidak boleh dilakukan
  ----------------------- ----------------------- -----------------------
  Controller              HTTP, binding form,     Mengubah saldo / aturan
                          memilih view            bisnis

  DTO                     Validasi dan bentuk     Menjadi tempat logika
                          data request/response   domain

  Service                 Use case, otorisasi     Mengakses request UI
                          domain, koordinasi      secara langsung
                          transaksi               

  Domain                  Aturan dan invariants   Mengurus HTML atau HTTP
                          rekening/transaksi      

  Repository              Query dan persistensi   Memutuskan aturan
                                                  bisnis

  Security                Login, role, proteksi   Menggantikan
                          endpoint                pemeriksaan kepemilikan
                                                  rekening

  Thymeleaf/JS            Presentasi dan          Menjadi sumber
                          interaksi UI            kebenaran saldo
  -----------------------------------------------------------------------

## 7. Alur Transaksi dan Integritas Data

### 7.1 Setor

Dalam service method yang diberi `@Transactional`: 1. Ambil rekening
dengan validasi kepemilikan dan status. 2. Validasi amount. 3. Jalankan
operasi credit pada domain. 4. Simpan perubahan rekening. 5. Simpan
transaksi DEPOSIT. 6. Commit jika semua langkah berhasil.

### 7.2 Tarik

Dalam satu transaksi database: 1. Ambil rekening milik nasabah dan
kunci/versi record. 2. Validasi status dan nominal. 3. Jalankan
`debit()`; domain menolak saldo yang tidak mencukupi atau melanggar
saldo minimum. 4. Simpan rekening dan transaksi WITHDRAWAL. 5. Commit.

### 7.3 Transfer internal

Dalam satu `@Transactional`: 1. Validasi request dan
idempotency/reference key. 2. Ambil rekening sumber dan tujuan dengan
strategi penguncian konsisten. 3. Pastikan keduanya aktif dan berbeda.
4. Pastikan pengguna berhak menggunakan rekening sumber. 5. Validasi
nominal dan saldo. 6. Debit sumber. 7. Credit tujuan. 8. Simpan catatan
TRANSFER_OUT (nominal + biaya, `balance_after`) dan TRANSFER_IN yang
terhubung lewat satu baris `transfers` dan `transfer_reference`. 9. Commit seluruh perubahan. 10. Jika ada exception,
rollback seluruh perubahan.

**Konkurensi (keputusan):** gunakan **pessimistic lock**
(`@Lock(PESSIMISTIC_WRITE)` / `SELECT ... FOR UPDATE`) pada semua jalur
yang mengubah saldo, dan tetap simpan `@Version` sebagai pengaman kedua.
Pada transfer, kunci kedua rekening **berurutan menurut `id` menaik**
(bukan "sumber dulu"), sehingga A→B dan B→A bersamaan tidak deadlock.
Jangan membaca saldo lalu mengubahnya tanpa kunci. Catat bahwa
database H2 tidak mereproduksi perilaku lock MySQL: uji konkurensi
harus memakai MySQL asli (Testcontainers).

**Idempotensi (keputusan):** form membawa `idempotency_key` acak (UUID).
Di awal transaksi, `INSERT` ke tabel `idempotency_keys` (unik pada
`user_id, key`). Bila duplikat, jangan proses ulang: tampilkan bukti
transaksi yang sudah ada. Kunci ini **berbeda** dari `reference_number`
transaksi (yang dibuat server). Berlaku juga untuk setor dan tarik,
bukan hanya transfer.

**Kegagalan tidak disimpan di ledger.** Karena rollback membatalkan semua
insert dalam transaksi yang sama, baris `FAILED` tidak bisa ikut
tersimpan kecuali memakai `REQUIRES_NEW`. Untuk MVP: hanya `SUCCESS`
yang disimpan; kegagalan dicatat ke log.

## 8. Rancangan Database

Satu skema, konsisten dengan PRD §9 (inkonsistensi `created_by` /
`account_id` / `transaction_out_id` pada versi 1.0 sudah disatukan).

### `users`
`id` PK, `email` UNIQUE NOT NULL, `password_hash` NOT NULL, `role`
(`CUSTOMER`/`ADMIN`) NOT NULL, `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
`failed_login_attempts` INT NOT NULL DEFAULT 0, `locked_until` DATETIME NULL,
`created_at`, `updated_at`.

### `customers`
`id` PK, `user_id` FK UNIQUE, `full_name`, `phone`, `address` NULL, `created_at`, `updated_at`.

### `accounts`
`id` PK, `account_number` CHAR(10) UNIQUE NOT NULL, `customer_id` FK NOT NULL,
`account_type` (`SAVINGS`/`CURRENT`/`DEPOSIT`) NOT NULL (discriminator),
`balance` DECIMAL(19,0) NOT NULL, `status` NOT NULL, `version` BIGINT NOT NULL,
`tenor_months` INT NULL, `maturity_date` DATE NULL, `created_at`, `updated_at`.
Constraint: `CHECK (balance >= 0)`;
`CHECK (account_type <> 'DEPOSIT' OR (tenor_months IS NOT NULL AND maturity_date IS NOT NULL))`.
(MySQL 8.0.16+ menegakkan CHECK.)

### `transactions`
`id` PK, `reference_number` UNIQUE NOT NULL, `account_id` FK NOT NULL,
`type` (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER_IN`, `TRANSFER_OUT`, `DISBURSEMENT`) NOT NULL,
`amount` DECIMAL(19,0) NOT NULL, `fee` DECIMAL(19,0) NOT NULL DEFAULT 0,
`balance_after` DECIMAL(19,0) NOT NULL, `status` NOT NULL, `description`,
`created_by` FK users, `created_at` DATETIME(3) (UTC).
`CHECK (amount > 0)`. Index `(account_id, created_at DESC)`.
Setiap baris punya `reference_number` sendiri (dua baris transfer
tidak boleh berbagi nilai karena kolom UNIQUE); keduanya dihubungkan
lewat tabel `transfers`.

### `transfers`
`id` PK, `transfer_reference` UNIQUE NOT NULL, `transaction_out_id` FK UNIQUE,
`transaction_in_id` FK UNIQUE, `source_account_id` FK, `destination_account_id` FK,
`amount` DECIMAL(19,0) NOT NULL, `created_at`.
`CHECK (source_account_id <> destination_account_id)`.

### `idempotency_keys`
`id` PK, `user_id` FK, `idem_key` CHAR(36) NOT NULL, `request_hash`, `transaction_id` NULL FK,
`created_at`. UNIQUE (`user_id`, `idem_key`).

### `audit_logs`
`id` PK, `actor_user_id` FK, `action`, `target_type`, `target_id`, `reason` NULL,
`created_at`, `metadata` JSON tanpa data rahasia. Index `(actor_user_id, created_at)`.

Flyway: `V1__schema.sql`, `V2__seed_admin_and_demo.sql` (admin + 2-3 nasabah
demo dengan rekening tiap jenis). Ledger bersifat *append-only*: tidak ada
`UPDATE`/`DELETE` pada `transactions` dari kode aplikasi.

## 9. Keamanan

-   Spring Security untuk autentikasi dan role-based access.
-   BCrypt atau Argon2 untuk hash password.
-   Proteksi CSRF untuk form berbasis session.
-   Validasi server-side untuk semua input.
-   Otorisasi kepemilikan rekening pada setiap use case.
-   Jangan percaya `customerId`, `accountId`, saldo, atau role yang
    dikirim browser tanpa verifikasi server.
-   Gunakan parameterized query/JPA, bukan membangun SQL dari input.
-   Simpan konfigurasi rahasia di environment.
-   Jangan tampilkan stack trace atau detail database kepada pengguna.
-   Catat aktivitas admin dan kejadian keamanan tanpa mencatat
    password/token.
-   HTTPS pada deployment.

## 10. Penanganan Error

Gunakan exception domain yang spesifik, misalnya: -
`AccountNotFoundException` - `InactiveAccountException` -
`InsufficientBalanceException` - `InvalidAmountException` -
`SameAccountTransferException` - `WithdrawalNotAllowedException` -
`DuplicateRequestException` - `AccountAccessDeniedException`
(**jangan** dinamai `AccessDeniedException`: bentrok dengan kelas
Spring Security `org.springframework.security.access.AccessDeniedException`)

Controller/exception handler menerjemahkan exception menjadi pesan yang
mudah dimengerti dan status HTTP yang sesuai. Detail teknis dicatat di
log internal, bukan ditampilkan ke pengguna.

## 11. Strategi Pengujian

-   **Unit test domain:** saldo tidak negatif, validasi amount, aturan
    tiap jenis rekening.
-   **Unit test service:** setor, tarik, transfer, otorisasi,
    idempotensi.
-   **Integration test repository:** mapping dan constraint database.
-   **Integration test transaksi:** rollback saat kredit/debit gagal.
-   **Security test:** akses customer/admin dan akses lintas pemilik.
-   **UI test/manual test:** form, pesan validasi, responsive layout.
-   **Concurrency test:** dua penarikan bersamaan tidak boleh membuat
    saldo negatif; transfer A→B dan B→A bersamaan tidak deadlock.
    Jalankan pada MySQL asli via Testcontainers, bukan H2.
-   **Idempotensi:** dua request dengan `idempotency_key` sama menghasilkan satu transaksi.
-   **Deposito:** jatuh tempo diuji lewat `Clock`/`today` yang diinjeksi.

Skenario minimum transfer (tambahan: 9. transfer dari Giro memotong biaya; 10. transfer ke Deposito ditolak): 1. Transfer valid. 2. Saldo tidak cukup. 3.
Tujuan tidak ditemukan. 4. Tujuan nonaktif. 5. Transfer ke rekening
sendiri. 6. Nominal nol/negatif. 7. Request dikirim dua kali. 8.
Kegagalan setelah debit harus me-rollback debit.

## 12. Urutan Implementasi

1.  Inisialisasi Spring Boot dan koneksi MySQL.
2.  Buat entity/domain dan migration database.
3.  Implementasikan empat pilar OOP pada domain rekening.
4.  Buat repository dan unit test domain.
5.  Buat autentikasi dan otorisasi.
6.  Implementasikan service setor/tarik/transfer dengan transaksi
    database.
7.  Buat controller dan DTO.
8.  Buat halaman Thymeleaf mengikuti design system.
9.  Buat dashboard admin dan nasabah.
10. Jalankan integration/security/concurrency tests.
11. Dokumentasikan cara menjalankan aplikasi dan akun demo.

## 13. Risiko dan Mitigasi

  -----------------------------------------------------------------------
  Risiko                              Mitigasi
  ----------------------------------- -----------------------------------
  Saldo berubah sebagian saat         Database transaction dan rollback
  transfer                            

  Dua transaksi bersamaan menyebabkan Locking atau optimistic version
  saldo salah                         

  Pengguna mengakses rekening orang   Otorisasi kepemilikan di service
  lain                                

  Request terkirim berulang           Idempotency key

  Log membocorkan data rahasia        Redaction dan aturan logging

  Hierarki OOP dibuat hanya demi      Gunakan inheritance untuk variasi
  tugas                               rekening yang benar-benar berbeda

  Scope terlalu besar                 Selesaikan MVP terlebih dahulu
  -----------------------------------------------------------------------
