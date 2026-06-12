package com.innowise.orderservice.infrastructure.persistence.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "application.security")
public record SecurityProperties(
        String jwksUrl,
        Paths paths
) {


    public record Paths(
            Set<String> publicPaths){}

}

