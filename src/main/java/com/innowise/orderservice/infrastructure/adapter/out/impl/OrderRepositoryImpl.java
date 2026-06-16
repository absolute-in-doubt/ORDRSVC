package com.innowise.orderservice.infrastructure.adapter.out.impl;

import com.innowise.orderservice.domain.model.Order;
import com.innowise.orderservice.domain.port.out.CustomOrderRepository;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderRepositoryImpl implements CustomOrderRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Order> findAll(
            Specification<Order> spec,
            Pageable pageable,
            String entityGraphName) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> root = cq.from(Order.class);

        Predicate predicate = spec.toPredicate(root, cq, cb);

        if (predicate != null) {
            cq.where(predicate);
        }


        if (pageable.getSort().isSorted()) {
            List<jakarta.persistence.criteria.Order> orders = pageable.getSort().stream()
                    .map(order -> order.isAscending()
                            ? cb.asc(root.get(order.getProperty()))
                            : cb.desc(root.get(order.getProperty())))
                    .toList();

            cq.orderBy(orders);
        }

        TypedQuery<Order> query = em.createQuery(cq);

        EntityGraph<?> graph = em.getEntityGraph(entityGraphName);

        query.setHint("jakarta.persistence.fetchgraph", graph);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Order> content = query.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Order> countRoot = countQuery.from(Order.class);

        Predicate countPredicate =
                spec.toPredicate(countRoot, countQuery, cb);

        countQuery.select(cb.count(countRoot));

        if (countPredicate != null) {
            countQuery.where(countPredicate);
        }

        Long total = em.createQuery(countQuery)
                .getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
