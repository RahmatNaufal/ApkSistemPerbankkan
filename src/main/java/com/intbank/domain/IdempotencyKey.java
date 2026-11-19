package com.intbank.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Kunci pengirim ulang request (berbeda dengan reference number transaksi). */
@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "idem_key", nullable = false, length = 36)
    private String key;

    @Column(name = "request_hash", nullable = false, length = 128)
    private String requestHash;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected IdempotencyKey() {
    }

    public IdempotencyKey(Long userId, String key, String requestHash, Long transactionId) {
        this.userId = userId;
        this.key = key;
        this.requestHash = requestHash;
        this.transactionId = transactionId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getKey() {
        return key;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
