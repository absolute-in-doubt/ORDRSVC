package com.innowise.orderservice.infrastructure.security.model;

public record JwtUserDetails(
        Long userId,
        String login
) {
}
