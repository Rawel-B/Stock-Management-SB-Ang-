package com.dsm.repositories;

import com.dsm.entities.Carrier;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarrierRepository extends MongoRepository<Carrier, String> {
    List<Carrier> getCarrierByName(String name);
    List<Carrier> getAllActiveCarriers();
    
}