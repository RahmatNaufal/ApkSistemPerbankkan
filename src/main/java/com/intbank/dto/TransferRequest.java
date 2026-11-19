package com.intbank.dto;

import jakarta.validation.constraints.NotBlank;

public class TransferRequest {

    @NotBlank(message = "Pilih rekening sumber.")
    private String sourceAccountNumber;

    @NotBlank(message = "Nomor rekening tujuan wajib diisi.")
    private String destinationAccountNumber;

    @NotBlank(message = "Nominal wajib diisi.")
    private String amount;

    @NotBlank(message = "Kunci permintaan tidak valid.")
    private String idempotencyKey;

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
