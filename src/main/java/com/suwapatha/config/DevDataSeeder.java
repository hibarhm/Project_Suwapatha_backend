package com.suwapatha.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
@Profile("dev") // Only runs when spring.profiles.active=dev
public class DevDataSeeder {

    // NOTE: Hospital admins are no longer seeded in dev.
    // Create them via the SUPER_ADMIN endpoint:
    // POST /api/super-admin/hospitals/{hospitalId}/admins
}