package com.bankflow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataSourceLogger implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment env = event.getEnvironment();

        String dbUrlEnv = env.getProperty("DB_URL", "NOT_SET");
        String dbUsernameEnv = env.getProperty("DB_USERNAME", "NOT_SET");
        String datasourceUrl = env.getProperty("spring.datasource.url", "NOT_SET");
        String datasourceUsername = env.getProperty("spring.datasource.username", "NOT_SET");
        String activeProfile = String.join(", ", env.getActiveProfiles());

        log.info("========================================");
        log.info("DATASOURCE CONFIGURATION (Early Startup)");
        log.info("========================================");
        log.info("Active Profile: {}", activeProfile.isEmpty() ? "default" : activeProfile);
        log.info("Environment Variables:");
        log.info("  DB_URL env var: {}", dbUrlEnv);
        log.info("  DB_USERNAME env var: {}", dbUsernameEnv);
        log.info("Resolved Spring Properties:");
        log.info("  spring.datasource.url: {}", datasourceUrl);
        log.info("  spring.datasource.username: {}", datasourceUsername);
        log.info("========================================");
    }
}

