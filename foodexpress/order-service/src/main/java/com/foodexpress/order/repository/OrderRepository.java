
package com.foodexpress.order.repository;

import com.foodexpress.common.enums.OrderStatus;
import com.foodexpress.order.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    Page<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);

    Page<Order> findByRestaurantIdOrderByCreatedAtDesc(String restaurantId, Pageable pageable);

    Page<Order> findByRestaurantIdAndStatusIn(String restaurantId, List<OrderStatus> statuses, Pageable pageable);

    List<Order> findByDeliveryPartnerIdAndStatusIn(String deliveryPartnerId, List<OrderStatus> statuses);

    Optional<Order> findByIdAndCustomerId(String id, String customerId);

    Optional<Order> findByIdAndRestaurantId(String id, String restaurantId);

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, Instant cutoff);

    long countByRestaurantIdAndCreatedAtAfter(String restaurantId, Instant after);
}
