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

    public InvoiceResponse addInvoice(InvoiceRequest request) {
        Order order = orderService.findById(request.getOrderId());

        if (order.getStatus() == Order.OrderStatus.cancelled) {
            throw new BusinessException("Cannot Invoice A Cancelled Order.");
        }
        if (order.getStatus() == Order.OrderStatus.pendingApproval) {
            throw new BusinessException("Only Validated, Ongoing, Or Delivered Orders Can Be Invoiced.");
        }

        BigDecimal totalCost = invoiceRepository.getInvoicesByOrderId(order.getId()).stream()
                .filter(invoice -> invoice.getStatus() == Invoice.InvoiceStatus.completed || invoice.getStatus() == Invoice.InvoiceStatus.processing || invoice.getStatus() == Invoice.InvoiceStatus.pending)
                .map(Invoice::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalCost.compareTo(order.getTotalAmount()) >= 0) {
            throw new BusinessException("The Invoicing For This Order Is Already Covered.");
        }

        Invoice invoice = Invoice.builder()
                .orderId(order.getId())
                .method(request.getMethod())
                .amount(request.getAmount())
                .invoicingDate(LocalDateTime.now())
                .status(Invoice.InvoiceStatus.pending)
                .transactionRef(request.getTransactionRef() != null ? request.getTransactionRef() : "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .remark(request.getRemark())
                .build();

        return toResponse(invoiceRepository.save(invoice));
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
        validateStatusChange(invoice.getStatus(), status);
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
    private void validateStatusChange(Invoice.InvoiceStatus current, Invoice.InvoiceStatus next) {
        if (current == next) {
            return;
        }
        boolean allowed = switch (current) {
            case pending -> next == Invoice.InvoiceStatus.processing || next == Invoice.InvoiceStatus.cancelled;
            case processing -> next == Invoice.InvoiceStatus.completed || next == Invoice.InvoiceStatus.failed || next == Invoice.InvoiceStatus.cancelled;
            case failed -> next == Invoice.InvoiceStatus.processing || next == Invoice.InvoiceStatus.cancelled;
            case completed -> next == Invoice.InvoiceStatus.refunded;
            case refunded, cancelled -> false;
        };
        if (!allowed) {
            throw new BusinessException("This Invoice Cannot Move To That Status.");
        }
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
}
