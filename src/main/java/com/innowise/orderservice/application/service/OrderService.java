package com.innowise.orderservice.application.service;

import com.innowise.orderservice.application.dto.CreateOrderRequestDto;
import com.innowise.orderservice.application.dto.FullOrderResponseDto;
import com.innowise.orderservice.application.dto.OrderFilter;
import com.innowise.orderservice.application.dto.UpdateOrderRequestDto;
import com.innowise.orderservice.domain.exception.ItemNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    FullOrderResponseDto createOrder(Long userId, CreateOrderRequestDto createOrderRequest) throws ItemNotFoundException;

    FullOrderResponseDto getOrderById(Long orderId);

    Page<FullOrderResponseDto> getOrderFilteredAndPaged(OrderFilter filter, Pageable pageable);

    List<FullOrderResponseDto> getOrdersVyUserId(Long userId);

    FullOrderResponseDto updateOrderById(Long orderId , UpdateOrderRequestDto updateOrderRequest);

    void deleteOrderById(Long orderId);
}