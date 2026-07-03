package com.innowise.orderservice.domain.exception;

public class OrderNotFoundException extends Exception {
    public OrderNotFoundException(Long orderId) {
        super("Failed to find an order with id: " + orderId);
    }
}
