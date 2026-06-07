package com.dsm.repositories;

import com.dsm.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    @Query("{ 'username': ?0 }")
    Optional<User> getUserByUsername(String username);
    @Query(value = "{ 'username': ?0 }", exists = true)
    boolean checkUsernameValidity(String username);
    @Query(value = "{ 'email': ?0 }", exists = true)
    boolean checkEmailValidity(String email);

}
