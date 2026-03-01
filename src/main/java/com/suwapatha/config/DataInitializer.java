/*package com.suwapatha.config;

import com.suwapatha.entity.User;
import com.suwapatha.entity.UserRole;
import com.suwapatha.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!dev") // Only run if NOT dev profile
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if admin user exists
        if (userRepository.findByEmail("admin@suwapatha.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@suwapatha.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setRole(UserRole.ADMIN);
            admin.setStatus("APPROVED");

            userRepository.save(admin);
            log.info("Admin user created: admin@suwapatha.com / admin123");
        }
    }
}*/