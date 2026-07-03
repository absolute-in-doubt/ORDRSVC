package com.innowise.orderservice.domain.port.in;

import jakarta.validation.Valid;
import com.innowise.orderservice.application.dto.CreateOrderRequestDto;
import com.innowise.orderservice.application.dto.FullOrderResponseDto;
import com.innowise.orderservice.application.dto.OrderFilter;
import com.innowise.orderservice.application.dto.UpdateOrderRequestDto;
import com.innowise.orderservice.domain.exception.ItemNotFoundException;
import com.innowise.orderservice.domain.exception.OrderNotFoundException;
import com.innowise.orderservice.domain.model.UserContext;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderController {

    ResponseEntity<FullOrderResponseDto> createOrder(UserContext userContext, @Valid CreateOrderRequestDto createOrderRequestDto) throws ItemNotFoundException;

    ResponseEntity<FullOrderResponseDto> getOrderById(UserContext userContext, Long orderId) throws OrderNotFoundException;

    ResponseEntity<Page<FullOrderResponseDto>> getOrdersFilteredAndPaged(UserContext userContext, OrderFilter orderFilter, Pageable pageable);

    ResponseEntity<List<FullOrderResponseDto>> getOrdersByUserId(UserContext userContext, Long userId);

    ResponseEntity<FullOrderResponseDto> updateOrderById(UserContext userContext, Long orderId, @Valid UpdateOrderRequestDto updateOrderRequestDto) throws OrderNotFoundException, ItemNotFoundException;

    ResponseEntity<Void> deleteById(UserContext userContext, Long orderId) throws OrderNotFoundException;


}
