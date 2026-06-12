package com.innowise.orderservice.domain.port.out;

import com.innowise.orderservice.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


public interface CustomOrderRepository {
    Page<Order> findAll(
            Specification<Order> specification,
            Pageable pageable,
            String entityGraphName
    );
}
