package com.innowise.orderservice.application.dto;

public record UserInfoResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        boolean active
) {
}
