# Sistem Perbankan — INT BANK

> **Simulasi perbankan untuk pembelajaran.** Tidak terhubung ke bank, jaringan
> pembayaran, ATM, atau rekening sungguhan. Jangan gunakan uang/data pribadi nyata.

**Judul utama:** Sistem Perbankan · **Branding:** INT BANK

## Status pengembangan

| Tahap | Isi | Status |
|---|---|---|
| 1 | Lapisan domain OOP + unit test + demo mandiri | **Selesai** |
| 2 | Spring Boot, Spring Security, Thymeleaf, MySQL XAMPP + halaman sesuai PRD | **Selesai** |

Tahap 1 sengaja dibuat **tanpa framework** agar empat pilar OOP mudah dibaca
dan diuji langsung dengan `javac`. Salinan domain murni tetap dipakai oleh
`demo\jalankan-demo.bat` (folder `domain-reference/`), sehingga demo Tahap 1
tidak berubah meskipun kelas domain di `src/main/java` kini beranotasi JPA.

## Menjalankan aplikasi web (Tahap 2)

### Opsi A — MySQL / XAMPP (profil default)

Pastikan XAMPP MySQL sudah berjalan, lalu:

```powershell
.\mvnw.cmd clean spring-boot:run
```

Buka <http://localhost:8080>. Database `int_bank` dibuat otomatis, lalu
skema dibuat oleh Flyway (`src/main/resources/db/migration/V1__schema.sql`).
Kredensial default: user `root`, password kosong, port `3306`.

Nilai bisa ditimpa lewat environment variable:

```powershell
$env:DB_URL='jdbc:mysql://localhost:3306/int_bank?createDatabaseIfNotExist=true'
$env:DB_USER='root'
$env:DB_PASSWORD=''
.\mvnw.cmd spring-boot:run
```

> XAMPP memakai **MariaDB**, jadi `application.properties` memakai
> `MariaDBDialect`. `MySQLDialect` menghasilkan klausa `FOR UPDATE OF` yang
> ditolak MariaDB saat transfer/setor/tarik (row locking).

### Opsi B — Demo H2 tanpa XAMPP

```powershell
.\mvnw.cmd clean spring-boot:run -Dspring-boot.run.profiles=demo
```

Memakai H2 in-memory; data contoh diisi ulang setiap kali aplikasi start.
Konsol H2: <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:mem:intdemo`).

### Akun demo

| Email | Password | Role |
|---|---|---|
| `admin@intbank.ac.id` | `Admin123!` | ADMIN |
| `budi@intbank.ac.id` | `Nasabah123!` | CUSTOMER |
| `siti@intbank.ac.id` | `Nasabah123!` | CUSTOMER |

Pengunjung juga bisa mendaftar sendiri lewat `/register` (langsung mendapat
tabungan dengan saldo awal simulasi Rp500.000).

### Halaman yang tersedia

- **Publik:** beranda, login, registrasi, bedah OOP (simulator polimorfisme),
  halaman error 403/404/500.
- **Nasabah:** dashboard, daftar/detail rekening, buka rekening (Tabungan/Giro/
  Deposito), setor, tarik, transfer + bukti transaksi, riwayat dengan filter &
  paginasi, profil.
- **Admin:** dashboard statistik, daftar & detail nasabah, ubah status nasabah
  (tercatat di audit log), monitoring transaksi, audit log.

Fitur keamanan: BCrypt, Spring Security form login, pembagian role
`/nasabah/**` dan `/admin/**`, CSRF pada semua form, serta penguncian akun
sementara setelah 5 kali salah password (15 menit).

Aturan transfer memakai kunci baris (`PESSIMISTIC_WRITE`), ledger berpasangan,
dan **idempotensi**: mengirim ulang permintaan dengan kunci yang sama
mengembalikan nomor bukti yang sama tanpa mendebet saldo dua kali.

## Pengujian

```powershell
.\mvnw.cmd test
```

Hasil: **38 tests run, 0 failures, 0 errors** — 34 unit test domain (Tahap 1)
dan 4 integration test Tahap 2 (`Stage2IntegrationTest`: halaman publik,
login & role guard, transfer + idempotensi, perubahan status + audit).

## Struktur proyek

```text
prjek/
├── pom.xml                  # Spring Boot 4.1.2, Java 17, Flyway starter
├── mvnw / mvnw.cmd          # Maven wrapper 3.9.11
├── README.md
├── docs/
│   ├── PRD.md               # rancangan produk (dari file Anda)
│   ├── architecture.md      # rancangan arsitektur
│   ├── designSystem.md      # rancangan tampilan
│   └── REVIEW.md            # catatan hasil review rancangan
├── demo/
│   ├── jalankan-demo.bat    # demo 22 pemeriksaan tanpa library
│   └── jalankan-test.bat
├── domain-reference/        # salinan domain murni (dipakai demo)
└── src/
    ├── main/java/com/intbank/
    │   ├── domain/          # 19 kelas domain (JPA annotation)
    │   ├── config/          # Security, auth handlers, demo seed, properties
    │   ├── repository/      # 7 Spring Data JPA repository
    │   ├── dto/             # 6 form request + validasi
    │   ├── service/         # 10 application service
    │   ├── web/             # controller, view mapper, advice, error controller
    │   └── util/            # FormatUtil
    ├── main/resources/
    │   ├── application.properties          # default MySQL/XAMPP
    │   ├── application-demo.properties     # H2 demo
    │   ├── db/migration/V1__schema.sql     # skema Flyway
    │   ├── templates/      # 26 file Thymeleaf (fragment, publik, nasabah, admin)
    │   └── static/         # app.css, app.js
    └── test/
        ├── java/com/intbank/domain/        # 34 unit test JUnit 5
        ├── java/com/intbank/web/           # 4 integration test MockMvc
        ├── java/DomainDemo.java            # 22 pemeriksaan tanpa library
        └── resources/application-test.properties  # H2 untuk integration test
```

## Tahap 1 — demo tanpa framework

### Cara menjalankan

Klik dua kali `demo\jalankan-demo.bat`, atau dari PowerShell:

```powershell
.\demo\jalankan-demo.bat
```

Hasil: **22 pemeriksaan lulus** (sesuai bukti pada `docs/REVIEW.md`).

Manualnya:

```powershell
javac -encoding UTF-8 -d out domain-reference\src\com\intbank\domain\*.java
javac -encoding UTF-8 -cp out -d out domain-reference\DomainDemo.java
java -cp out DomainDemo
```

`JALANKAN.bat` di root menyediakan menu untuk demo, test JUnit, dan aplikasi web.

## Empat pilar OOP — matriks keterlacakan

| Pilar | Kode sumber | Unit test | Tampilan Tahap 2 |
|---|---|---|---|
| Enkapsulasi | `Money`, `Account` (saldo private, tanpa setter) | `MoneyTest`, `AccountTest` | Pesan validasi form |
| Abstraksi | `Account` (abstract + template method + hook) | `AccountTest`, `AccountTypeRulesTest` | Bedah OOP: diagram kelas |
| Pewarisan | `SavingsAccount`, `CurrentAccount`, `DepositAccount` | `AccountTypeRulesTest` | Detail rekening: aturan tiap jenis |
| Polimorfisme | `Account.withdraw()` via tipe induk, `interestProjection()` | `TransferServiceTest`, `AccountTypeRulesTest` | Simulator Bedah OOP |

Aturan penting yang dijaga di domain:

- Saldo hanya berubah lewat `credit()` / `withdraw()` yang memvalidasi dulu.
- `Nominal > 0`, rekening harus `ACTIVE`, saldo tidak boleh negatif.
- Tabungan: minimum Rp50.000, tanpa biaya, bunga proyeksi 3% p.a.
- Giro: tanpa minimum, biaya Rp2.500 per tarik/transfer keluar, tanpa bunga.
- Deposito: tanpa setoran tambahan, cair penuh setelah jatuh tempo, bunga proyeksi 5% p.a.
- Bunga hanya **proyeksi**, tidak diposting ke saldo (sesuai batas MVP).
- Transfer: validasi sebelum mutasi, kepemilikan rekening sumber dicek (anti-IDOR).
- Waktu (`today`) disuntikkan sebagai parameter agar jatuh tempo bisa diuji.

## Catatan teknis

- Spring Boot 4 memisahkan auto-konfigurasi Flyway ke
  `spring-boot-starter-flyway` (dependency `flyway-core` saja tidak cukup).
- `@PathVariable`/`@RequestParam` tanpa nama eksplisit memerlukan
  `<parameters>true</parameters>` pada `maven-compiler-plugin`.
- Form Thymeleaf bergantung pada CSRF yang disuntikkan Spring Security;
  pengujian HTTP manual harus memakai sesi + token yang sama untuk GET dan POST.

## Sumber rancangan

Seluruh isi `docs/` adalah salinan dari file `int-bank-v2.zip` yang Anda kirim.
Rincian fitur, aturan bisnis, skema database, dan design system ada di sana.
