package com.intbank.domain;

/** Role pengguna. Role selalu ditentukan server, bukan dari form registrasi. */
public enum UserRole {
    CUSTOMER("ROLE_CUSTOMER"),
    ADMIN("ROLE_ADMIN");

    private final String authority;

    UserRole(String authority) {
        this.authority = authority;
    }

    public String authority() {
        return authority;
    }
}
