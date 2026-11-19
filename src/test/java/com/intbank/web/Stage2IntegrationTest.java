package com.intbank.web;

import com.intbank.domain.Account;
import com.intbank.domain.AccountStatus;
import com.intbank.domain.Money;
import com.intbank.domain.SavingsAccount;
import com.intbank.domain.TransactionType;
import com.intbank.repository.AccountRepository;
import com.intbank.repository.AuditLogRepository;
import com.intbank.repository.CustomerRepository;
import com.intbank.repository.TransactionRepository;
import com.intbank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pengujian alur web Tahap 2: keamanan, transfer, idempotensi, dan audit. */
@SpringBootTest
@ActiveProfiles("test")
class Stage2IntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    @DisplayName("Halaman publik menampilkan branding INT BANK")
    void publicPagesRenderBranding() throws Exception {
        for (String url : new String[]{"/", "/login", "/register", "/bedah-oop"}) {
            mockMvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("INT BANK")));
        }
    }

    @Test
    @DisplayName("Login nasabah berhasil dan hak akses dibatasi per role")
    void customerLoginAndRoleGuards() throws Exception {
        MvcResult login = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "budi@intbank.ac.id")
                        .param("password", "Nasabah123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/nasabah/dashboard"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(session).isNotNull();
        mockMvc.perform(get("/nasabah/dashboard").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/dashboard").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error/403"));

        mockMvc.perform(get("/admin/dashboard").with(user("admin@intbank.ac.id").roles("ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/nasabah/dashboard").with(user("admin@intbank.ac.id").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error/403"));
    }

    @Test
    @DisplayName("Transfer tercatat sekali dan idempotensi mengembalikan bukti yang sama")
    void transferIsIdempotent() throws Exception {
        Account sumber = savingsOf("budi@intbank.ac.id");
        Account tujuan = savingsOf("siti@intbank.ac.id");
        String key = UUID.randomUUID().toString();

        String first = transfer(sumber, tujuan, key);
        String second = transfer(sumber, tujuan, key);

        assertThat(first).contains("/nasabah/transaksi/bukti/");
        assertThat(second).isEqualTo(first);

        String reference = first.substring(first.lastIndexOf('/') + 1);
        mockMvc.perform(get(first).with(user("budi@intbank.ac.id").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(reference)));

        long ledgerCount = transactionRepository.findAll().stream()
                .filter(t -> t.getType() == TransactionType.TRANSFER_OUT)
                .filter(t -> sumber.id().equals(t.getAccountId()))
                .filter(t -> Money.of(100_000).equals(t.getAmount()))
                .count();
        assertThat(ledgerCount).isEqualTo(1);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("Perubahan status admin tercatat di audit dan langsung terlihat di rekening")
    void adminStatusChangeIsAudited() throws Exception {
        Long sitiUserId = userRepository.findByEmail("siti@intbank.ac.id").orElseThrow().getId();
        Long sitiCustomerId = customerRepository.findByUserId(sitiUserId).orElseThrow().getId();

        mockMvc.perform(post("/admin/nasabah/{id}/status", sitiCustomerId)
                        .with(user("admin@intbank.ac.id").roles("ADMIN"))
                        .with(csrf())
                        .param("status", "BLOCKED")
                        .param("reason", "Uji integrasi"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/nasabah/" + sitiCustomerId));

        assertThat(accountRepository.findAllByOwnerCustomerId(sitiCustomerId))
                .allSatisfy(a -> assertThat(a.status()).isEqualTo(AccountStatus.BLOCKED));

        boolean audited = auditLogRepository
                .findByTargetTypeAndTargetIdOrderByCreatedAtDesc("CUSTOMER", String.valueOf(sitiCustomerId))
                .stream()
                .anyMatch(log -> "STATUS_CHANGE".equals(log.getAction())
                        && log.getReason() != null && log.getReason().contains("Uji integrasi"));
        assertThat(audited).isTrue();
    }

    private String transfer(Account sumber, Account tujuan, String key) throws Exception {
        return mockMvc.perform(post("/nasabah/transaksi/transfer")
                        .with(user("budi@intbank.ac.id").roles("CUSTOMER"))
                        .with(csrf())
                        .param("sourceAccountNumber", sumber.accountNumber())
                        .param("destinationAccountNumber", tujuan.accountNumber())
                        .param("amount", "100000")
                        .param("idempotencyKey", key))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();
    }

    private Account savingsOf(String email) {
        Long userId = userRepository.findByEmail(email).orElseThrow().getId();
        Long customerId = customerRepository.findByUserId(userId).orElseThrow().getId();
        return accountRepository.findAllByOwnerCustomerId(customerId).stream()
                .filter(SavingsAccount.class::isInstance)
                .findFirst()
                .orElseThrow();
    }
}
