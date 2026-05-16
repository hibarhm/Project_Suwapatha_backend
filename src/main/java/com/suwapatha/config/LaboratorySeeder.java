package com.suwapatha.config;

import com.suwapatha.entity.User;
import com.suwapatha.entity.UserRole;
import com.suwapatha.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LaboratorySeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("lab@suwapatha.com")) {
            User labAssistant = new User();
            labAssistant.setEmail("lab@suwapatha.com");
            labAssistant.setPassword(passwordEncoder.encode("lab123"));
            labAssistant.setFirstName("John");
            labAssistant.setLastName("Lab");
            labAssistant.setRole(UserRole.LABORATORY);
            labAssistant.setStatus("APPROVED"); // Assuming lab staff need approval too or are auto-approved for dev
            
            userRepository.save(labAssistant);
            log.info("Laboratory assistant user seeded: lab@suwapatha.com / lab123");
        }
    }
}
