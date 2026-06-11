package com.dsm.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.dsm.entities.Location;

public interface LocationRepository extends MongoRepository<Location, String> {
    @Query("{ 'name': ?0 }")
    Optional<Location> getLocationByName(String name);
    @Query("{ 'code': ?0 }")
    Optional<Location> getLocationByCode(String code);
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<Location> getLocationsByName(String name);

}
