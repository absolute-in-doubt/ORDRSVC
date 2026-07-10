package com.innowise.orderservice.application.service;

import com.innowise.orderservice.application.dto.*;
import com.innowise.orderservice.domain.exception.ItemNotFoundException;
import com.innowise.orderservice.domain.exception.OrderNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderApplicationService {

    FullOrderResponseDto createOrder(Long userId, CreateOrderRequestDto createOrderRequest) throws ItemNotFoundException;

    FullOrderResponseDto getOrderById(Long userId, Long orderId) throws OrderNotFoundException;

    Page<FullOrderResponseDto> getOrderFilteredAndPaged(Long userId, OrderFilter filter, Pageable pageable);

    List<FullOrderResponseDto> getOrdersByUserId(Long userId, Long userIdPathParamValue);

    FullOrderResponseDto updateOrderById(Long userId, Long orderId , UpdateOrderRequestDto updateOrderRequest) throws OrderNotFoundException, ItemNotFoundException;

    void deleteOrderById(Long userId, Long orderId) throws OrderNotFoundException;

    void updateOrderStatus(UpdateOrderStatusRequestDto requestDto) throws OrderNotFoundException;
}