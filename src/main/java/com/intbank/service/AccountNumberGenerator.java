package com.intbank.service;

import com.intbank.repository.AccountRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Nomor rekening 10 digit acak, bukan urutan yang mudah ditebak. */
@Component
public class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final AccountRepository accountRepository;

    public AccountNumberGenerator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public String generate() {
        for (int attempt = 0; attempt < 50; attempt++) {
            long value = 1_000_000_000L + (long) (RANDOM.nextDouble() * 9_000_000_000L);
            String candidate = String.valueOf(value);
            if (accountRepository.findByAccountNumber(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new IllegalStateException("Gagal membuat nomor rekening unik.");
    }
}
