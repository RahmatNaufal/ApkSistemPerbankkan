package com.intbank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ProfileRequest {

    @NotBlank(message = "Nama lengkap wajib diisi.")
    private String fullName;

    @NotBlank(message = "Nomor telepon wajib diisi.")
    @Pattern(regexp = "^[0-9+\\-\\s]{8,30}$", message = "Nomor telepon tidak valid.")
    private String phone;

    private String address;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
