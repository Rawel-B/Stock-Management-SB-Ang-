package com.dsm.repositories;

import com.dsm.entities.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    List<Invoice> getInvoicesByOrderId(String orderId);
    List<Invoice> getInvoicesByStatus(Invoice.InvoiceStatus invoiceStatus);

}