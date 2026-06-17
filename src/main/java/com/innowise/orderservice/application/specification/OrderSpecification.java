package com.innowise.orderservice.application.specification;

import com.innowise.orderservice.application.dto.OrderFilter;
import com.innowise.orderservice.domain.model.Order;
import com.innowise.orderservice.domain.model.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class OrderSpecification {

    public static Specification<Order> withCreationDate(LocalDateTime creationDate){
        return (root, query, cb) -> (creationDate == null)? null :
                cb.equal(root.get("createdAt"), creationDate);
    }

    public static Specification<Order> withAnyStatus(List<OrderStatus> statuses){
        return (root, query, cb) -> (statuses == null || statuses.isEmpty())? null :
                root.get("status").in(statuses);
    }

    public static Specification<Order> withDeletedFalse() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }

    public static Specification<Order> withUserId(Long userId) {
        return (root, query, cb) -> (userId == null) ? null :
                cb.equal(root.get("userId"), userId);
    }

    public static Specification<Order> fromFilter(OrderFilter filter){
        return Specification.allOf(
                withCreationDate(filter.creationDate()),
                withAnyStatus(filter.orderStatuses()),
                withDeletedFalse()
        );
    }

}
