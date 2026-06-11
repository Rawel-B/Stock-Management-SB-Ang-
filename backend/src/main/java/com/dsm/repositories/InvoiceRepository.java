package com.dsm.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.dsm.entities.Invoice;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    @Query("{ 'orderId': ?0 }")
    List<Invoice> getInvoicesByOrderId(String orderId);
    @Query("{ 'status': ?0 }")
    List<Invoice> getInvoicesByStatus(Invoice.InvoiceStatus invoiceStatus);

}
