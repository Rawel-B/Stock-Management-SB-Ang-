package com.dsm.repositories;

import com.dsm.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> getOrdersByCustomerId(String clientId);
    Page<Order> getOrdersByCustomerIdAndPage(String clientId, Pageable pageable);
    List<Order> getOrdersByStatus(Order.OrderStatus orderStatus);
    Long getOrdersCountByStatus(Order.OrderStatus orderStatus);
    Optional<Order> getOrdersByOrderNumber(String orderNumber);
    List<Order> getOrdersByDate(LocalDateTime startDate, LocalDateTime endDate);

}
