package com.bankflow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataSourceLogger {

    @Value("${spring.datasource.url:NOT_SET}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:NOT_SET}")
    private String datasourceUsername;

    @Value("${DB_URL:NOT_SET}")
    private String dbUrlEnv;

    @Value("${DB_USERNAME:NOT_SET}")
    private String dbUsernameEnv;

    @EventListener(ApplicationReadyEvent.class)
    public void logDataSourceConfiguration() {
        log.info("========================================");
        log.info("DATASOURCE CONFIGURATION");
        log.info("========================================");
        log.info("Environment Variables:");
        log.info("  DB_URL env var: {}", dbUrlEnv);
        log.info("  DB_USERNAME env var: {}", dbUsernameEnv);
        log.info("Resolved Spring Properties:");
        log.info("  spring.datasource.url: {}", datasourceUrl);
        log.info("  spring.datasource.username: {}", datasourceUsername);
        log.info("========================================");
    }
}

