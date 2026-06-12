package com.innowise.orderservice.infrastructure.adapter.in;

import com.innowise.orderservice.application.dto.CreateOrderRequestDto;
import com.innowise.orderservice.application.dto.FullOrderResponseDto;
import com.innowise.orderservice.application.dto.OrderFilter;
import com.innowise.orderservice.application.dto.UpdateOrderRequestDto;
import com.innowise.orderservice.application.service.OrderApplicationService;
import com.innowise.orderservice.domain.exception.ItemNotFoundException;
import com.innowise.orderservice.domain.exception.OrderNotFoundException;
import com.innowise.orderservice.domain.port.in.OrderController;
import com.innowise.orderservice.infrastructure.persistence.security.model.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class OrderControllerImpl implements OrderController {

    private final OrderApplicationService orderService;

    @Override
    @PostMapping
    public ResponseEntity<FullOrderResponseDto> createOrder(
            @AuthenticationPrincipal JwtUserDetails jwtUserDetails,
            @RequestBody CreateOrderRequestDto createOrderRequestDto)
            throws ItemNotFoundException {

        return ResponseEntity.ok(orderService.createOrder(jwtUserDetails.userId(), createOrderRequestDto));
    }

    @Override
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<FullOrderResponseDto> getOrderById(
            @AuthenticationPrincipal JwtUserDetails jwtUserDetails,
            @PathVariable("orderId") Long orderId)
            throws OrderNotFoundException {

        return ResponseEntity.ok(orderService.getOrderById(jwtUserDetails.userId(), orderId));
    }

    @Override
    @GetMapping("/orders")
    public ResponseEntity<Page<FullOrderResponseDto>> getOrdersFilteredAndPaged(
            @AuthenticationPrincipal JwtUserDetails jwtUserDetails,
            @ParameterObject OrderFilter orderFilter,
            @PageableDefault Pageable pageable) {

        return ResponseEntity.ok(orderService.getOrderFilteredAndPaged(jwtUserDetails.userId(), orderFilter, pageable));
    }

    @Override
    @GetMapping("/users/{userId}/orders")
    public ResponseEntity<List<FullOrderResponseDto>> getOrdersByUserId(
            @AuthenticationPrincipal JwtUserDetails jwtUserDetails,
            @PathVariable("userId") Long userIdPathParamValue
    ) {

        return ResponseEntity.ok(orderService.getOrdersByUserId(jwtUserDetails.userId(), userIdPathParamValue));
    }

    @Override
    @PutMapping("/orders/{orderId}")
    public ResponseEntity<FullOrderResponseDto> updateOrderById(
            @AuthenticationPrincipal JwtUserDetails jwtUserDetails,
            @PathVariable("orderId") Long orderId,
            @RequestBody UpdateOrderRequestDto updateOrderRequestDto) throws OrderNotFoundException {

        return ResponseEntity.ok(orderService.updateOrderById(jwtUserDetails.userId(), orderId, updateOrderRequestDto));
    }

    @Override
    public ResponseEntity<Void> deleteById(JwtUserDetails jwtUserDetails, Long orderId) throws OrderNotFoundException {

        orderService.deleteOrderById(jwtUserDetails.userId(), orderId);

        return ResponseEntity.noContent().build();
    }
}
