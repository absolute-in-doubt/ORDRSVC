package com.innowise.orderservice.domain.port.in;

import com.innowise.orderservice.application.dto.CreateOrderRequestDto;
import com.innowise.orderservice.application.dto.FullOrderResponseDto;
import com.innowise.orderservice.application.dto.OrderFilter;
import com.innowise.orderservice.application.dto.UpdateOrderRequestDto;
import com.innowise.orderservice.infrastructure.persistence.security.model.JwtUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderController {

    ResponseEntity<FullOrderResponseDto> createUser(JwtUserDetails jwtUserDetails, CreateOrderRequestDto createOrderRequestDto);

    ResponseEntity<FullOrderResponseDto> getOrderById(JwtUserDetails jwtUserDetails, Long orderId);

    ResponseEntity<Page<FullOrderResponseDto>> getOrdersFilteredAndPaged(JwtUserDetails jwtUserDetails, OrderFilter orderFilter, Pageable pageable);

    ResponseEntity<List<FullOrderResponseDto>> getOrdersByUserId(JwtUserDetails jwtUserDetails);

    ResponseEntity<FullOrderResponseDto> updateOrderById(JwtUserDetails jwtUserDetails, Long orderId, UpdateOrderRequestDto updateOrderRequestDto);

    ResponseEntity<Void> deleteById(JwtUserDetails jwtUserDetails, Long orderId);


}
