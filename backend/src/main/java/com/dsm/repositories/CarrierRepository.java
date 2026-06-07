package com.dsm.repositories;

import com.dsm.entities.Carrier;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarrierRepository extends MongoRepository<Carrier, String> {
    @Query("{ 'name': ?0 }")
    List<Carrier> getCarrierByName(String name);
    @Query("{ 'isActive': true }")
    List<Carrier> getAllActiveCarriers();
    
}
