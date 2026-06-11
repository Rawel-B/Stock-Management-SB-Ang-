package com.dsm.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.dsm.entities.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
    @Query("{ 'orderId': ?0 }")
    List<Product> getProductsByOrderId(String orderId);
    @Query(value = "{ 'orderId': ?0 }", delete = true)
    void deleteProductByOrderId(String orderId);

}
