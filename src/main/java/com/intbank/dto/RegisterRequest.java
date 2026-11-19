package com.intbank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Nama lengkap wajib diisi.")
    @Size(max = 150, message = "Nama terlalu panjang.")
    private String fullName;

    @NotBlank(message = "Email wajib diisi.")
    @Email(message = "Format email tidak valid.")
    private String email;

    @NotBlank(message = "Nomor telepon wajib diisi.")
    @Pattern(regexp = "^[0-9+\\-\\s]{8,30}$", message = "Nomor telepon tidak valid.")
    private String phone;

    @NotBlank(message = "Password wajib diisi.")
    @Size(min = 8, max = 100, message = "Password minimal 8 karakter.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Password harus mengandung huruf dan angka.")
    private String password;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
