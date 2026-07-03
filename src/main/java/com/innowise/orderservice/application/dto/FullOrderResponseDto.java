package com.innowise.orderservice.application.dto;

public record FullOrderResponseDto(
        UserInfoResponseDto userInfo,
        OrderResponseDto order
) {
}
