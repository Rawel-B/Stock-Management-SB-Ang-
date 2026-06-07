package com.dsm.repositories;

import com.dsm.entities.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    @Query("{ 'orderId': ?0 }")
    List<Invoice> getInvoicesByOrderId(String orderId);
    @Query("{ 'status': ?0 }")
    List<Invoice> getInvoicesByStatus(Invoice.InvoiceStatus invoiceStatus);

}
