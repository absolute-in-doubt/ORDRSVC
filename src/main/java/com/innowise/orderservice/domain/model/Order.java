package com.innowise.orderservice.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "orders")
@Entity
@EntityListeners({AuditingEntityListener.class})
@Data
public class Order {

    @Id
    private Long id;
    @Column(name="user_id")
    private Long userId;

    //private OrderStatus status;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    private boolean deleted;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}