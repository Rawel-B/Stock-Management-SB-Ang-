package com.dsm.repositories;

import com.dsm.entities.Stock;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends MongoRepository<Stock, String> {
    @Query("{ 'product': ?0 }")
    List<Stock> getStockByProduct(String product);
    @Query("{ 'productRef': ?0 }")
    Optional<Stock> getStockByProductRef(String productRef);

}
