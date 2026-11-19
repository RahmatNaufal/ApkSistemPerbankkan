package com.intbank.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Menghubungkan dua baris ledger (debit pengirim & kredit penerima). */
@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_reference", nullable = false, unique = true, length = 40)
    private String transferReference;

    @Column(name = "transaction_out_id", nullable = false, unique = true)
    private Long transactionOutId;

    @Column(name = "transaction_in_id", nullable = false, unique = true)
    private Long transactionInId;

    @Column(name = "source_account_id", nullable = false)
    private Long sourceAccountId;

    @Column(name = "destination_account_id", nullable = false)
    private Long destinationAccountId;

    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false, precision = 19, scale = 0)
    private Money amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Transfer() {
    }

    public Transfer(String transferReference, Long transactionOutId, Long transactionInId,
                    Long sourceAccountId, Long destinationAccountId, Money amount) {
        this.transferReference = transferReference;
        this.transactionOutId = transactionOutId;
        this.transactionInId = transactionInId;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTransferReference() {
        return transferReference;
    }

    public Long getTransactionOutId() {
        return transactionOutId;
    }

    public Long getTransactionInId() {
        return transactionInId;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    public Money getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
