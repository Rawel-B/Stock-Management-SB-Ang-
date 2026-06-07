package com.dsm.repositories;

import com.dsm.entities.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    @Query("{ 'orderId': ?0 }")
    List<Product> getProductsByOrderId(String orderId);
    @Query(value = "{ 'orderId': ?0 }", delete = true)
    void deleteProductByOrderId(String orderId);

}
