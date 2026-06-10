package com.innowise.orderservice.application.dto;

public record OrderItemResponseDto(
        Long orderItemId,
        Long itemId,
        String itemName,
        String itemPrice,
        Long quantity
) {
}
