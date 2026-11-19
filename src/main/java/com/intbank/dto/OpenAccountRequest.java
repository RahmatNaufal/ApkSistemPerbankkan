package com.intbank.dto;

import jakarta.validation.constraints.NotBlank;

public class OpenAccountRequest {

    @NotBlank(message = "Pilih jenis rekening.")
    private String type;

    private Integer tenorMonths;

    private String sourceAccountNumber;

    private String amount;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getTenorMonths() {
        return tenorMonths;
    }

    public void setTenorMonths(Integer tenorMonths) {
        this.tenorMonths = tenorMonths;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
