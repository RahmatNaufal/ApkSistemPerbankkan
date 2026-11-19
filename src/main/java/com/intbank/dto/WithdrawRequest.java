package com.intbank.dto;

import jakarta.validation.constraints.NotBlank;

public class WithdrawRequest {

    @NotBlank(message = "Pilih rekening sumber.")
    private String accountNumber;

    @NotBlank(message = "Nominal wajib diisi.")
    private String amount;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
