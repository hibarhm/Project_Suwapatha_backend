package com.suwapatha.config;

import com.suwapatha.entity.User;
import com.suwapatha.entity.UserRole;
import com.suwapatha.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class DevSuperAdminSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_EMAIL = "superadmin@suwapatha.com";
    private static final String DEFAULT_PASSWORD = "SuperAdmin@123";

    @Bean
    @Order(1)
    public CommandLineRunner seedSuperAdmin() {
        return args -> {
            String email = System.getenv("SUPER_ADMIN_EMAIL");
            String password = System.getenv("SUPER_ADMIN_PASSWORD");

            if (email == null || email.isBlank()) email = DEFAULT_EMAIL;
            if (password == null || password.isBlank()) password = DEFAULT_PASSWORD;

            if (userRepository.findByEmail(email).isPresent()) {
                log.info("SUPER_ADMIN already exists: {}", email);
                return;
            }

            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setFirstName("Super");
            user.setLastName("Admin");
            user.setRole(UserRole.SUPER_ADMIN);
            user.setEnabled(true);
            user.setStatus("APPROVED");

            userRepository.save(user);
            log.info("SUPER_ADMIN seeded: {}", email);
        };
    }
}

