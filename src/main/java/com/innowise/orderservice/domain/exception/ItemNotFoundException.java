package com.innowise.orderservice.domain.exception;

public class ItemNotFoundException extends Exception {
    public ItemNotFoundException(Long id) {
        super("Failed to find an item with id: " + id);
    }
}
