package com.intbank.domain;

/**
 * Hierarki exception domain.
 *
 * <p><b>PEWARISAN:</b> semua kegagalan bisnis merupakan turunan
 * {@link DomainException}, sehingga lapisan aplikasi bisa menangani satu tipe
 * induk sekaligus, tetapi tetap bisa memberi pesan spesifik ke pengguna.
 */
public class DomainExceptions {

    /** Induk semua kegagalan aturan bisnis. */
    public static class DomainException extends RuntimeException {
        public DomainException(String message) {
            super(message);
        }
    }

    public static class InvalidAmountException extends DomainException {
        public InvalidAmountException() {
            super("Nominal harus lebih besar dari nol.");
        }
    }

    public static class InactiveAccountException extends DomainException {
        public InactiveAccountException() {
            super("Rekening tidak aktif.");
        }
    }

    public static class InsufficientBalanceException extends DomainException {
        public InsufficientBalanceException(String message) {
            super(message);
        }
    }

    public static class WithdrawalNotAllowedException extends DomainException {
        public WithdrawalNotAllowedException(String message) {
            super(message);
        }
    }

    public static class SameAccountTransferException extends DomainException {
        public SameAccountTransferException() {
            super("Tidak dapat transfer ke rekening sendiri.");
        }
    }

    /**
     * Sengaja tidak bernama {@code AccessDeniedException} agar tidak bentrok
     * dengan kelas Spring Security yang bernama sama.
     */
    public static class AccountAccessDeniedException extends DomainException {
        public AccountAccessDeniedException() {
            super("Anda tidak berhak mengakses rekening ini.");
        }
    }
}
