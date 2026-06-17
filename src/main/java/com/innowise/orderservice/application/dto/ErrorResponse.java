package com.innowise.orderservice.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(LocalDateTime timestamp, String errorMessage) {
}
