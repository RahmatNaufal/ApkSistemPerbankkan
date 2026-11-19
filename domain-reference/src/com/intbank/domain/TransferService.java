package com.intbank.domain;

import java.time.LocalDate;

import static com.intbank.domain.DomainExceptions.DomainException;

/**
 * Aturan transfer di level domain.
 *
 * <p><b>POLIMORFISME:</b> method ini hanya bergantung pada tipe {@link Account},
 * bukan pada subclass konkretnya. Perilaku (biaya Giro, saldo minimum Tabungan,
 * penolakan Deposito) ditentukan oleh objek yang sebenarnya.
 *
 * <p>Semua validasi dijalankan <b>sebelum</b> mutasi, sehingga kegagalan tidak
 * pernah meninggalkan saldo setengah berubah. Di aplikasi Spring Boot kelak,
 * method ini dipanggil dari service {@code @Transactional} dengan row locking.
 */
public final class TransferService {

    public record Receipt(Money amount, Money fee, Money senderBalance, Money receiverBalance) {
    }

    public Receipt transfer(Account from, Account to, Money amount,
                            Long actingCustomerId, LocalDate today) {
        if (from == null || to == null) {
            throw new DomainException("Rekening sumber dan tujuan wajib diisi.");
        }
        if (!from.ownerCustomerId().equals(actingCustomerId)) {
            throw new DomainExceptions.AccountAccessDeniedException();
        }
        if (from.accountNumber().equals(to.accountNumber())) {
            throw new DomainExceptions.SameAccountTransferException();
        }

        // validasi dulu, baru mutasi (validasi tidak mengubah state)
        from.validateWithdraw(amount, today);
        to.validateCredit(amount);

        Money fee = from.withdraw(amount, today);
        to.credit(amount);

        return new Receipt(amount, fee, from.balance(), to.balance());
    }
}
