/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package tools.dynamia.modules.saas.migration.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Spring Boot auto-configuration for the Tenant Mobility module.
 *
 * <p>Registers:
 * <ul>
 *   <li>A migration-specific {@link ObjectMapper} (qualified name: {@code migrationObjectMapper}).</li>
 *   <li>The default {@link tools.dynamia.modules.saas.migration.api.AccountMigrationService} implementation.</li>
 *   <li>Ensures the output directory exists at startup.</li>
 * </ul>
 *
 * @author Mario Serrano Leones
 */
@Configuration
@EnableConfigurationProperties(AccountMigrationProperties.class)
public class AccountMigrationConfig {

    private final AccountMigrationProperties properties;

    public AccountMigrationConfig(AccountMigrationProperties properties) {
        this.properties = properties;
        initOutputDirectory();
    }

    /**
     * Dedicated Jackson {@link ObjectMapper} for the migration pipelines.
     *
     * <p>Configured to:
     * <ul>
     *   <li>Support Java 8+ date/time types via {@link JavaTimeModule}.</li>
     *   <li>Not fail on unknown properties during import.</li>
     *   <li>Not fail on empty beans.</li>
     *   <li>Exclude null values from output (smaller files).</li>
     * </ul>
     *
     * <p>This bean is named {@code migrationObjectMapper} so it does not conflict
     * with any other {@code ObjectMapper} in the application context.
     */
    @Bean("migrationObjectMapper")
    @ConditionalOnMissingBean(name = "migrationObjectMapper")
    public ObjectMapper migrationObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private void initOutputDirectory() {
        try {
            Path dir = Path.of(properties.getOutputDirectory());
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Cannot create migration output directory: " + properties.getOutputDirectory(), e);
        }
    }
}


