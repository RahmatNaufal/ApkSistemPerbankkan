package com.intbank.domain;

/** Status rekening. Transisinya dijaga oleh {@link Account}. */
public enum AccountStatus {
    ACTIVE("Aktif"),
    INACTIVE("Nonaktif"),
    BLOCKED("Diblokir"),
    CLOSED("Ditutup");

    private final String label;

    AccountStatus(String label) {
        this.label = label;
    }

    /** Label Bahasa Indonesia untuk ditampilkan di UI. */
    public String label() {
        return label;
    }
}
