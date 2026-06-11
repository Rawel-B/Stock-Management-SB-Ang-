package com.dsm.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.dsm.entities.User;

public interface UserRepository extends MongoRepository<User, String> {
    @Query("{ 'username': ?0 }")
    Optional<User> getUserByUsername(String username);
    @Query("{ 'email': ?0 }")
    Optional<User> getUserByEmail(String email);
    @Query(value = "{ 'username': ?0 }", exists = true)
    boolean checkUsernameValidity(String username);
    @Query(value = "{ 'email': ?0 }", exists = true)
    boolean checkEmailValidity(String email);

}
