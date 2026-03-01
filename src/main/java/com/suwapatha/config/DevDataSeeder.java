package com.suwapatha.config;

import com.suwapatha.entity.Hospital;
import com.suwapatha.entity.User;
import com.suwapatha.entity.UserRole;
import com.suwapatha.repository.HospitalRepository;
import com.suwapatha.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("dev") // Only runs when spring.profiles.active=dev
public class DevDataSeeder {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Order(1)
    public CommandLineRunner seedAdminsForHospitals() {
        return args -> {
            log.info("╔════════════════════════════════════════════════════════════╗");
            log.info("║        Suwapatha Dev Data Seeder - Multi-Hospital         ║");
            log.info("╚════════════════════════════════════════════════════════════╝");

            // Check if admins already exist
            long existingAdminCount = userRepository.findAll().stream()
                    .filter(u -> UserRole.ADMIN.equals(u.getRole()))
                    .count();

            if (existingAdminCount > 0) {
                log.info("Admins already exist ({}), skipping seed", existingAdminCount);
                log.info("To re-seed, delete all admins from database first");
                log.info("════════════════════════════════════════════════════════════");
                return;
            }

            // Get all hospitals
            List<Hospital> hospitals = hospitalRepository.findAll();

            if (hospitals.isEmpty()) {
                log.error("No hospitals found in database!");
                log.error("Please seed hospitals first before running this script");
                log.info("════════════════════════════════════════════════════════════");
                return;
            }

            log.info("Found {} hospitals in database", hospitals.size());
            log.info("Creating admin users for each hospital...");
            log.info("────────────────────────────────────────────────────────────");

            int successCount = 0;
            int failCount = 0;

            // Create an admin for each hospital
            for (int i = 0; i < hospitals.size(); i++) {
                Hospital hospital = hospitals.get(i);

                try {
                    // Generate unique email based on hospital name
                    String sanitizedName = hospital.getName()
                            .toLowerCase()
                            .replaceAll("[^a-z0-9]", "")
                            .replaceAll("\\s+", "");

                    if (sanitizedName.length() > 20) {
                        sanitizedName = sanitizedName.substring(0, 20);
                    }

                    String email = "admin." + sanitizedName + "@suwapatha.com";

                    if (userRepository.findByEmail(email).isPresent()) {
                        email = "admin" + (i + 1) + "@suwapatha.com";
                    }

                    // Create admin user
                    User admin = new User();
                    admin.setEmail(email);
                    admin.setPassword(passwordEncoder.encode("Admin@123"));
                    admin.setFirstName(hospital.getDistrict() + " Admin");
                    admin.setLastName(hospital.getName());
                    admin.setRole(UserRole.ADMIN);
                    admin.setEnabled(true);
                    admin.setStatus("APPROVED");

                    admin = userRepository.save(admin);

                    // Assign admin to hospital
                    hospital.setAdminId(admin.getId());
                    hospitalRepository.save(hospital);

                    successCount++;

                    log.info("[{}/{}] Created: {}",
                            successCount, hospitals.size(), email);
                    log.info("   └─ Hospital: {} ({})",
                            hospital.getName(), hospital.getDistrict());

                } catch (Exception e) {
                    failCount++;
                    log.error("Failed to create admin for: {}", hospital.getName());
                    log.error("   Error: {}", e.getMessage());
                }
            }

            log.info("────────────────────────────────────────────────────────────");
            log.info("Seeding Summary:");
            log.info("Successfully created: {} admins", successCount);
            if (failCount > 0) {
                log.warn(" Failed: {} admins", failCount);
            }
            log.info("════════════════════════════════════════════════════════════");
            log.info("Default Login Credentials:");
            log.info("   Email: admin.<hospitalname>@suwapatha.com");
            log.info("   Password: Admin@123");
            log.info("────────────────────────────────────────────────────────────");
            log.info("Example logins:");

            List<Hospital> exampleHospitals = hospitals.stream().limit(5).toList();
            for (int i = 0; i < exampleHospitals.size(); i++) {
                Hospital h = exampleHospitals.get(i);
                if (h.getAdminId() != null) {
                    int finalI = i;
                    userRepository.findById(h.getAdminId()).ifPresent(admin -> {
                        log.info("   {}. Email: {}", (finalI + 1), admin.getEmail());
                        log.info("      Hospital: {}", h.getName());
                    });
                }
            }

            if (hospitals.size() > 5) {
                log.info("   ... and {} more", hospitals.size() - 5);
            }

            log.info("════════════════════════════════════════════════════════════");
            log.info("✨ Multi-hospital system ready for testing!");
            log.info("════════════════════════════════════════════════════════════");
        };
    }

    @Bean
    @Order(2)
    public CommandLineRunner verifyMappings() {
        return args -> {
            List<Hospital> hospitalsWithoutAdmins = hospitalRepository.findAll().stream()
                    .filter(h -> h.getAdminId() == null || h.getAdminId().isEmpty())
                    .toList();

            if (!hospitalsWithoutAdmins.isEmpty()) {
                log.warn("Warning: {} hospitals without assigned admins:",
                        hospitalsWithoutAdmins.size());
                hospitalsWithoutAdmins.forEach(h ->
                        log.warn("   - {} ({})", h.getName(), h.getId())
                );
            }

            List<User> adminsWithoutHospitals = userRepository.findAll().stream()
                    .filter(u -> UserRole.ADMIN.equals(u.getRole()))
                    .filter(u -> hospitalRepository.findByAdminId(u.getId()).isEmpty())
                    .toList();

            if (!adminsWithoutHospitals.isEmpty()) {
                log.warn("Warning: {} admins without assigned hospitals:",
                        adminsWithoutHospitals.size());
                adminsWithoutHospitals.forEach(a ->
                        log.warn("   - {} ({})", a.getEmail(), a.getId())
                );
            }
        };
    }
}