package com.intbank.config;

import com.intbank.domain.TransferService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Mendaftarkan layanan domain murni sebagai bean Spring tanpa mengubah kelas domain. */
@Configuration
public class DomainBeanConfig {

    @Bean
    public TransferService transferService() {
        return new TransferService();
    }
}
