package com.innowise.orderservice.infrastructure.persistence.security.model;

public record JwtUserDetails(
        Long userId,
        String login
) {
}
