package com.example.wallet.config;

import com.example.wallet.model.enums.Role;
import com.example.wallet.model.implementations.Support;
import com.example.wallet.repository.SupportRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdminUser(SupportRepository supportRepository, BCryptPasswordEncoder passwordEncoder) {
        return args -> {
            final String adminEmail = "admin@bunchpay.local";
            if (supportRepository.findByEmail(adminEmail).isEmpty()) {
                Support admin = new Support();
                admin.setFirstName("Admin");
                admin.setLastName("BunchPay");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("Admin123!"));
                admin.setRole(Role.ADMIN);
                supportRepository.save(admin);
                System.out.println(
                        "[DataInitializer] Admin user created: " + adminEmail + " (default password: Admin123!)");
            } else {
                System.out.println("[DataInitializer] Admin user already exists: " + adminEmail);
            }
        };
    }
}
