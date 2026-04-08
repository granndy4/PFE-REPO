package com.wtm.fuelvoucher.config;

import java.util.Locale;

import com.wtm.fuelvoucher.Enums.Role;
import com.wtm.fuelvoucher.Entities.UserAccount;
import com.wtm.fuelvoucher.Repositories.UserAccountRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapAdminConfig {

    @Bean
    public CommandLineRunner bootstrapAdmin(UserAccountRepository userAccountRepository,
                                            PasswordEncoder passwordEncoder,
                                            @Value("${app.bootstrap-admin.enabled:true}") boolean enabled,
                                            @Value("${app.bootstrap-admin.name:Admin}") String adminName,
                                            @Value("${app.bootstrap-admin.email:admin@wtm.local}") String adminEmail,
                                            @Value("${app.bootstrap-admin.password:Admin@12345}") String adminPassword) {
        return args -> {
            if (!enabled) {
                return;
            }

            String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
            if (userAccountRepository.existsByEmail(normalizedEmail)) {
                return;
            }

            UserAccount admin = new UserAccount();
            admin.setName(adminName.trim());
            admin.setEmail(normalizedEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userAccountRepository.save(admin);
        };
    }
}




