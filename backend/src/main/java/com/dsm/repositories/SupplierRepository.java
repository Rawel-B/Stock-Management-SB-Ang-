package com.dsm.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.dsm.entities.Supplier;

public interface SupplierRepository extends MongoRepository<Supplier, String> {
    @Query("{ 'name': ?0 }")
    List<Supplier> getSupplierByName(String name);
    @Query(value = "{ 'email': ?0 }", exists = true)
    boolean checkEmailValidity(String email);
    @Query("{ 'isActive': true }")
    List<Supplier> getAllActiveSuppliers();

}
