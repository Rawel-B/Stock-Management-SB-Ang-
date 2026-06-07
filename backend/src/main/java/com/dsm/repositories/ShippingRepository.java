package com.dsm.repositories;

import com.dsm.entities.Shipping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShippingRepository extends MongoRepository<Shipping, String> {
    @Query("{ 'orderId': ?0 }")
    List<Shipping> getShippingsByOrderId(String orderId);
    @Query("{ 'carrierId': ?0 }")
    List<Shipping> getShippingsByCarrierId(String carrierId);
    @Query("{ 'status': ?0 }")
    List<Shipping> getShippingsByStatus(Shipping.ShippingStatus shippingStatus);

}
