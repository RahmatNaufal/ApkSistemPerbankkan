package com.intbank.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/** Batas transaksi dan kebijakan login dari application.properties. */
@ConfigurationProperties(prefix = "intbank")
public record BankingProperties(
        BigDecimal maxTransactionAmount,
        BigDecimal maxDailyWithdrawal,
        Login login) {

    public record Login(int maxAttempts, int lockMinutes) {
    }
}
