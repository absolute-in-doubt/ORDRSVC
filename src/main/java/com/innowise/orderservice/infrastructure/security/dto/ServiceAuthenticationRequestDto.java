package com.innowise.orderservice.infrastructure.security.dto;

public record ServiceAuthenticationRequestDto(
        Long clientId,
        String clientLogin,
        String clientSecret
) {
}
