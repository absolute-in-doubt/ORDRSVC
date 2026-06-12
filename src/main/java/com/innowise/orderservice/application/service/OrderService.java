package com.innowise.orderservice.application.service;

import com.innowise.orderservice.application.dto.CreateOrderRequestDto;
import com.innowise.orderservice.application.dto.FullOrderResponseDto;
import com.innowise.orderservice.application.dto.OrderFilter;
import com.innowise.orderservice.application.dto.UpdateOrderRequestDto;
import com.innowise.orderservice.domain.exception.ItemNotFoundException;
import com.innowise.orderservice.domain.exception.OrderNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    FullOrderResponseDto createOrder(Long userId, CreateOrderRequestDto createOrderRequest) throws ItemNotFoundException;

    FullOrderResponseDto getOrderById(Long userId, Long orderId) throws OrderNotFoundException;

    Page<FullOrderResponseDto> getOrderFilteredAndPaged(Long userId, OrderFilter filter, Pageable pageable);

    List<FullOrderResponseDto> getOrdersByUserId(Long userId);

    FullOrderResponseDto updateOrderById(Long userId, Long orderId , UpdateOrderRequestDto updateOrderRequest) throws OrderNotFoundException;

    void deleteOrderById(Long userId, Long orderId) throws OrderNotFoundException;
}