package com.dsm.repositories;

import com.dsm.entities.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    List<Customer> getCustomerByName(String name);
    Optional<Customer> getCustomerByEmail(String email);
    boolean checkEmailValidity(String email);

}