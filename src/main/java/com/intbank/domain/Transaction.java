package com.intbank.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Catatan ledger. Baris yang dibuat tidak pernah diubah/dihapus aplikasi. */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_number", nullable = false, unique = true, length = 40)
    private String referenceNumber;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false, precision = 19, scale = 0)
    private Money amount;

    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false, precision = 19, scale = 0)
    private Money fee = Money.ZERO;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 0)
    private Money balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.SUCCESS;

    @Column(length = 255)
    private String description;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Transaction() {
    }

    public Transaction(String referenceNumber, Long accountId, TransactionType type, Money amount,
                       Money fee, Money balanceAfter, String description, Long createdBy) {
        this.referenceNumber = referenceNumber;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.fee = fee == null ? Money.ZERO : fee;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public Long getAccountId() {
        return accountId;
    }

    public TransactionType getType() {
        return type;
    }

    public Money getAmount() {
        return amount;
    }

    public Money getFee() {
        return fee;
    }

    public Money getBalanceAfter() {
        return balanceAfter;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
