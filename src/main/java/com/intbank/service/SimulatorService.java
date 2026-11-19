package com.intbank.service;

import com.intbank.domain.Account;
import com.intbank.domain.CurrentAccount;
import com.intbank.domain.DepositAccount;
import com.intbank.domain.DomainExceptions.DomainException;
import com.intbank.domain.Money;
import com.intbank.domain.SavingsAccount;
import com.intbank.util.FormatUtil;
import com.intbank.web.Views.SimulasiItem;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulasi OOP untuk halaman "Bedah OOP" — murni memakai kelas domain,
 * tidak menyentuh database dan tidak mengubah saldo asli.
 */
@Service
public class SimulatorService {

    /** Contoh akun dibuat dengan nomor simulasi dan id pemilik fiktif. */
    public List<SimulasiItem> simulate(String saldoInput, String nominalInput) {
        Money saldo;
        Money nominal;
        try {
            saldo = FormatUtil.parseAmount(saldoInput);
            nominal = FormatUtil.parseAmount(nominalInput);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Masukkan saldo dan nominal berupa angka yang valid.");
        }

        LocalDate today = LocalDate.now();
        List<Account> contoh = List.of(
                new SavingsAccount("SIM-TABUNGAN", 0L, saldo),
                new CurrentAccount("SIM-GIRO", 0L, saldo),
                new DepositAccount("SIM-DEPOSITO", 0L, saldo, 12, today)
        );

        List<SimulasiItem> hasil = new ArrayList<>();
        for (Account akun : contoh) {
            String status;
            String alasan;
            String biaya;
            String saldoAkhir;
            try {
                Money fee = akun.withdraw(nominal, today);
                status = "Diperbolehkan";
                alasan = "Aturan rekening terpenuhi.";
                biaya = FormatUtil.rupiah(fee);
                saldoAkhir = FormatUtil.rupiah(akun.balance());
            } catch (DomainException e) {
                status = "Ditolak";
                alasan = e.getMessage();
                biaya = "-";
                saldoAkhir = FormatUtil.rupiah(saldo);
            }
            hasil.add(new SimulasiItem(
                    akun.typeName(),
                    status,
                    alasan,
                    biaya,
                    saldoAkhir,
                    FormatUtil.rupiah(akun.interestProjection(12))));
        }
        return hasil;
    }
}
