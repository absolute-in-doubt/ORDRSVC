package com.innowise.orderservice.domain.port.in;

import com.innowise.orderservice.application.dto.CreateOrderRequestDto;
import com.innowise.orderservice.application.dto.FullOrderResponseDto;
import com.innowise.orderservice.application.dto.OrderFilter;
import com.innowise.orderservice.application.dto.UpdateOrderRequestDto;
import com.innowise.orderservice.domain.exception.ItemNotFoundException;
import com.innowise.orderservice.domain.exception.OrderNotFoundException;
import com.innowise.orderservice.infrastructure.security.model.JwtUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderController {

    ResponseEntity<FullOrderResponseDto> createOrder(JwtUserDetails jwtUserDetails, CreateOrderRequestDto createOrderRequestDto) throws ItemNotFoundException;

    ResponseEntity<FullOrderResponseDto> getOrderById(JwtUserDetails jwtUserDetails, Long orderId) throws OrderNotFoundException;

    ResponseEntity<Page<FullOrderResponseDto>> getOrdersFilteredAndPaged(JwtUserDetails jwtUserDetails, OrderFilter orderFilter, Pageable pageable);

    ResponseEntity<List<FullOrderResponseDto>> getOrdersByUserId(JwtUserDetails jwtUserDetails, Long userId);

    ResponseEntity<FullOrderResponseDto> updateOrderById(JwtUserDetails jwtUserDetails, Long orderId, UpdateOrderRequestDto updateOrderRequestDto) throws OrderNotFoundException, ItemNotFoundException;

    ResponseEntity<Void> deleteById(JwtUserDetails jwtUserDetails, Long orderId) throws OrderNotFoundException;


}
