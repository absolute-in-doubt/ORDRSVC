package com.innowise.orderservice.infrastructure.adapter.in;

import com.innowise.orderservice.application.dto.CreateOrderRequestDto;
import com.innowise.orderservice.application.dto.FullOrderResponseDto;
import com.innowise.orderservice.application.dto.OrderFilter;
import com.innowise.orderservice.application.dto.UpdateOrderRequestDto;
import com.innowise.orderservice.application.service.OrderApplicationService;
import com.innowise.orderservice.domain.exception.ItemNotFoundException;
import com.innowise.orderservice.domain.exception.OrderNotFoundException;
import com.innowise.orderservice.domain.model.UserContext;
import com.innowise.orderservice.domain.port.in.OrderController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class OrderControllerImpl implements OrderController {

    private final OrderApplicationService orderService;

    @Override
    @PostMapping("/order")
    public ResponseEntity<FullOrderResponseDto> createOrder(
            @AuthenticationPrincipal UserContext userContext,
            @RequestBody @Valid CreateOrderRequestDto createOrderRequestDto)
            throws ItemNotFoundException {

        FullOrderResponseDto responseDto = orderService.createOrder(userContext.userId(), createOrderRequestDto);

        return ResponseEntity.created(URI.create("/api/v1/orders/" + responseDto.order().id()))
                .body(orderService.createOrder(userContext.userId(), createOrderRequestDto));
    }

    @Override
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<FullOrderResponseDto> getOrderById(
            @AuthenticationPrincipal UserContext userContext,
            @PathVariable("orderId") Long orderId)
            throws OrderNotFoundException {

        return ResponseEntity.ok(orderService.getOrderById(userContext.userId(), orderId));
    }

    @Override
    @GetMapping("/orders")
    public ResponseEntity<Page<FullOrderResponseDto>> getOrdersFilteredAndPaged(
            @AuthenticationPrincipal UserContext userContext,
            @ParameterObject OrderFilter orderFilter,
            @PageableDefault Pageable pageable) {

        return ResponseEntity.ok(orderService.getOrderFilteredAndPaged(userContext.userId(), orderFilter, pageable));
    }

    @Override
    @GetMapping("/users/{userId}/orders")
    public ResponseEntity<List<FullOrderResponseDto>> getOrdersByUserId(
            @AuthenticationPrincipal UserContext userContext,
            @PathVariable("userId") Long userIdPathParamValue
    ) {

        return ResponseEntity.ok(orderService.getOrdersByUserId(userContext.userId(), userIdPathParamValue));
    }

    @Override
    @PutMapping("/orders/{orderId}")
    public ResponseEntity<FullOrderResponseDto> updateOrderById(
            @AuthenticationPrincipal UserContext userContext,
            @PathVariable("orderId") Long orderId,
            @RequestBody @Valid UpdateOrderRequestDto updateOrderRequestDto) throws OrderNotFoundException, ItemNotFoundException {

        return ResponseEntity.ok(orderService.updateOrderById(userContext.userId(), orderId, updateOrderRequestDto));
    }

    @Override
    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<Void> deleteById(
            @AuthenticationPrincipal UserContext userContext,
            @PathVariable("orderId") Long orderId) throws OrderNotFoundException {

        orderService.deleteOrderById(userContext.userId(), orderId);

        return ResponseEntity.noContent().build();
    }
}
