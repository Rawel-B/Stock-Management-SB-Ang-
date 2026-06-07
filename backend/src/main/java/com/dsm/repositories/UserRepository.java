package com.dsm.repositories;

import com.dsm.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> getUserByUsername(String username);
    boolean checkUsernameValidity(String username);
    boolean checkEmailValidity(String email);

}