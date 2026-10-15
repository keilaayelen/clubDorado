package com.clubdorado.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "goldenclub")
public record AppProperties(
    String adminUser,
    String adminPassword,
    String whatsappPhone,
    String whatsappMessage,
    String uploadDir
) {}
