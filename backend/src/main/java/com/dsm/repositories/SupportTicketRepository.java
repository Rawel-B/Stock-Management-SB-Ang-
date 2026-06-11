package com.dsm.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.dsm.entities.SupportTicket;

public interface SupportTicketRepository extends MongoRepository<SupportTicket, String> {
    @Query("{ '$or': [ { 'subject': { '$regex': ?0, '$options': 'i' } }, { 'requesterName': { '$regex': ?0, '$options': 'i' } }, { 'requesterEmail': { '$regex': ?0, '$options': 'i' } } ] }")
    List<SupportTicket> findByCriteria(String criteria);
}
