package com.dsm.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.dsm.entities.Order;

public interface OrderRepository extends MongoRepository<Order, String> {
    @Query("{ 'customerId': ?0 }")
    List<Order> getOrdersByCustomerId(String customerId);
    @Query("{ 'supplierId': ?0 }")
    List<Order> getOrdersBySupplierId(String supplierId);
    @Query("{ 'customerId': ?0 }")
    Page<Order> getOrdersByCustomerIdAndPage(String customerId, Pageable pageable);
    @Query("{ 'status': ?0 }")
    List<Order> getOrdersByStatus(Order.OrderStatus orderStatus);
    @Query(value = "{ 'status': ?0 }", count = true)
    Long getOrdersCountByStatus(Order.OrderStatus orderStatus);
    @Query("{ 'orderNumber': ?0 }")
    Optional<Order> getOrdersByOrderNumber(String orderNumber);
    @Query("{ 'orderDate': { $gte: ?0, $lte: ?1 } }")
    List<Order> getOrdersByDate(LocalDateTime startDate, LocalDateTime endDate);

}
