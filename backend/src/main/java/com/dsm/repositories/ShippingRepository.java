package com.dsm.repositories;

import com.dsm.entities.Shipping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingRepository extends MongoRepository<Shipping, String> {
    List<Shipping> getShippingsByOrderId(String orderId);
    List<Shipping> getShippingsByCarrierId(String carrierId);
    List<Shipping> getShippingsByStatus(Shipping.ShippingStatus shippingStatus);

}