package com.dsm.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.dsm.entities.Carrier;

public interface CarrierRepository extends MongoRepository<Carrier, String> {
    @Query("{ 'name': ?0 }")
    List<Carrier> getCarrierByName(String name);
    @Query("{ 'isActive': true }")
    List<Carrier> getAllActiveCarriers();
    
}
