package com.intbank.domain;

/** Jenis catatan transaksi (append-only ledger). */
public enum TransactionType {
    DEPOSIT("Setor dana"),
    WITHDRAWAL("Tarik dana"),
    TRANSFER_IN("Transfer masuk"),
    TRANSFER_OUT("Transfer keluar"),
    DISBURSEMENT("Pencairan deposito"),
    OPEN_ACCOUNT("Buka rekening"),
    ADJUSTMENT("Penyesuaian");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
