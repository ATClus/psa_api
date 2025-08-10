package com.clusterat.psa_api.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class SeedDataConfig {

    private static final Logger log = LoggerFactory.getLogger(SeedDataConfig.class);

    private final DatabaseSeeder databaseSeeder;

    public SeedDataConfig(DatabaseSeeder databaseSeeder) {
        this.databaseSeeder = databaseSeeder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application is ready, starting data seeding process...");
        try {
            databaseSeeder.seedData();
        } catch (Exception e) {
            log.error("Failed to seed database on application startup", e);
            // Don't throw exception to prevent application from failing to start
        }
    }
}