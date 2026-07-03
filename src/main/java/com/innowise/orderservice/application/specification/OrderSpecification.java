package com.innowise.orderservice.application.specification;

import com.innowise.orderservice.application.dto.OrderFilter;
import com.innowise.orderservice.domain.model.Order;
import com.innowise.orderservice.domain.model.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class OrderSpecification {

    public static Specification<Order> withCreationDateAfter(LocalDateTime creationDateTo){
        return (root, query, cb) -> (creationDateTo == null)? null :
                cb.lessThanOrEqualTo(root.get("createdAt"), creationDateTo);
    }

    public static Specification<Order> withCreationDateBefore(LocalDateTime creationDateFrom){
        return (root, query, cb) -> (creationDateFrom == null)? null :
                cb.greaterThanOrEqualTo(root.get("createdAt"), creationDateFrom);
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
                withCreationDateBefore(filter.creationDateTo()),
                withCreationDateAfter(filter.creationDateFrom()),
                withAnyStatus(filter.orderStatuses()),
                withDeletedFalse()
        );
    }

}
