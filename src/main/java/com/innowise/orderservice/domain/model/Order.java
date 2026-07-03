package com.innowise.orderservice.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "orders")
@NamedEntityGraph(
        name = "order-with-items",
        attributeNodes = {
                @NamedAttributeNode(
                        value = "items",
                        subgraph = "items-subgraph"
                )
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "items-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("item")
                        }
                )
        }
)
@Entity
@EntityListeners({AuditingEntityListener.class})
@Getter @Setter @ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Order {

    public Order(){
        this.items = new ArrayList<>();
    }

    @SequenceGenerator(name = "order_gen", sequenceName = "order_seq")

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(generator = "order_gen")
    private Long id;
    @Column(name="user_id")
    private Long userId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    private boolean deleted;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
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