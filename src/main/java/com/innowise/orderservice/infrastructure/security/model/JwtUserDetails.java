package com.innowise.orderservice.infrastructure.security.model;

import com.innowise.orderservice.domain.model.UserContext;

public record JwtUserDetails(
        Long userId,
        String login
) implements UserContext {
}
