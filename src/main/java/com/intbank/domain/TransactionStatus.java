package com.intbank.domain;

/** Status transaksi. MVP hanya menyimpan SUCCESS; lainnya disiapkan untuk future work. */
public enum TransactionStatus {
    PENDING("Diproses"),
    SUCCESS("Berhasil"),
    FAILED("Gagal"),
    REVERSED("Dibalik");

    private final String label;

    TransactionStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
