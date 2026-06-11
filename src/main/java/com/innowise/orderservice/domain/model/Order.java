package com.innowise.orderservice.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "orders")
@Entity
@EntityListeners({AuditingEntityListener.class})
@Data
public class Order {

    public Order(){
        this.items = new ArrayList<>();
    }

    @SequenceGenerator(name = "order_gen", sequenceName = "order_seq")

    @Id
    @GeneratedValue(generator = "order_gen")
    private Long id;
    @Column(name="user_id")
    private Long userId;

    private OrderStatus status;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    private boolean deleted;

    @OneToMany(mappedBy = "order", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<OrderItems> items;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public void addOrderItems(OrderItems items){
        this.items.add(items);
    }
}