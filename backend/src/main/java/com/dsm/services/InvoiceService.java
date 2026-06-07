package com.dsm.services;

import com.dsm.dto.request.RequestDTO.InvoiceRequest;
import com.dsm.dto.response.ResponseDTO.InvoiceResponse;
import com.dsm.entities.*;
import com.dsm.exception.*;
import com.dsm.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final OrderService orderService;

    //#region Main
    public InvoiceResponse addInvoice(InvoiceRequest request) {
        Order order = orderService.findById(request.getOrderId());

        if (order.getStatus() == Order.OrderStatus.cancelled) {
            throw new BusinessException("Cannot Invoice a Cancelled Order");
        }

        BigDecimal totalCost = invoiceRepository.getInvoicesByOrderId(order.getId()).stream()
                .filter(invoice -> invoice.getStatus() == Invoice.InvoiceStatus.completed || invoice.getStatus() == Invoice.InvoiceStatus.processing)
                .map(Invoice::getAmount)
                .filter(montant -> montant != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalCost.compareTo(order.getTotalAmount()) >= 0) {
            throw new BusinessException("The Invoicing For This Order Is Already Finished.");
        }

        Invoice invoice = Invoice.builder()
                .orderId(order.getId())
                .method(request.getMethod())
                .amount(request.getAmount())
                .invoicingDate(LocalDateTime.now())
                .status(Invoice.InvoiceStatus.processing)
                .transactionRef(request.getTransactionRef() != null ? request.getTransactionRef() : "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .remark(request.getRemark())
                .build();

        invoice = invoiceRepository.save(invoice);
        invoice.setStatus(Invoice.InvoiceStatus.completed); // This Is Still Simulated, Needs To Be Checked Through A Real Service
        invoice = invoiceRepository.save(invoice);
        return toResponse(invoice);
    }
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByOrder(String orderId) {
        return invoiceRepository.getInvoicesByOrderId(orderId).stream().map(this::toResponse).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(String id) {
        return toResponse(findById(id));
    }
    public InvoiceResponse updateInvoiceStatus(String id, Invoice.InvoiceStatus status) {
        Invoice invoice = findById(id);
        invoice.setStatus(status);
        return toResponse(invoiceRepository.save(invoice));
    }
    public void deleteInvoice(String id) {
        Invoice invoice = findById(id);

        if (invoice.getStatus() == Invoice.InvoiceStatus.completed) {
            throw new BusinessException("Cannot Remove Completed Invoices.");
        }

        invoiceRepository.deleteById(id);
    }
    private Invoice findById(String id) {
        return invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice With ID " + id + " Was Not Found."));
    }
    private InvoiceResponse toResponse(Invoice invoice) {
        Order order = invoice.getOrderId() != null ? orderService.findById(invoice.getOrderId()) : null;
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .orderId(invoice.getOrderId())
                .orderNumber(order != null ? order.getOrderNumber() : null)
                .invoicingDate(invoice.getInvoicingDate())
                .invoiceStatus(invoice.getStatus())
                .invoicingMethod(invoice.getMethod())
                .amount(invoice.getAmount())
                .transactionRef(invoice.getTransactionRef())
                .remark(invoice.getRemark())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
    //#endregion Main

}
