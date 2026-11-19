package com.intbank.web;

/** Model tampilan: semua nilai uang/tanggal sudah diformat di server. */
public final class Views {

    private Views() {
    }

    public record AccountView(
            String accountNumber,
            String maskedNumber,
            String typeName,
            String statusLabel,
            String status,
            String balanceFormatted,
            String minimumBalanceFormatted,
            String feeFormatted,
            String interestRule,
            String maturityDateFormatted,
            String interestProjectionFormatted) {
    }

    public record TransactionView(
            String referenceNumber,
            String typeLabel,
            String amountFormatted,
            String feeFormatted,
            String balanceAfterFormatted,
            String createdAtFormatted,
            String statusLabel,
            String direction,
            String description,
            String accountNumber,
            String destinationMasked,
            String type,
            String status) {
    }

    public record CustomerView(
            Long id,
            String fullName,
            String email,
            String phone,
            String address,
            boolean enabled,
            long accountCount,
            String totalBalanceFormatted,
            String createdAtFormatted) {
    }

    public record AuditLogView(
            String actorEmail,
            String action,
            String targetType,
            String targetId,
            String reason,
            String createdAtFormatted) {
    }

    public record TransferView(
            String transferReference,
            String destinationAccountNumberMasked,
            String destinationName) {
    }

    public record SimulasiItem(
            String jenis,
            String status,
            String alasan,
            String biaya,
            String saldoAkhir,
            String bunga) {
    }

    public record StatsView(
            String totalCustomers,
            String totalAccounts,
            String totalTransactions,
            String totalBalanceFormatted) {
    }
}
