package com.dsm.repositories;

import com.dsm.entities.Supplier;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupplierRepository extends MongoRepository<Supplier, String> {
    @Query("{ 'name': ?0 }")
    List<Supplier> getSupplierByName(String name);
    @Query(value = "{ 'email': ?0 }", exists = true)
    boolean checkEmailValidity(String email);
    @Query("{ 'isActive': true }")
    List<Supplier> getAllActiveSuppliers();

}
