package com.dsm.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.dsm.entities.Customer;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    @Query("{ 'name': ?0 }")
    List<Customer> getCustomerByName(String name);
    @Query("{ 'email': ?0 }")
    Optional<Customer> getCustomerByEmail(String email);
    @Query(value = "{ 'email': ?0 }", exists = true)
    boolean checkEmailValidity(String email);

}
