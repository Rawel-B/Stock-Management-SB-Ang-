package com.dsm.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dsm.dto.request.RequestDTO.InvoiceRequest;
import com.dsm.dto.response.ResponseDTO.InvoiceResponse;
import com.dsm.entities.Invoice;
import com.dsm.services.InvoiceService;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoicing Management")
public class InvoiceController {
    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<InvoiceResponse> createNewInvoice(@Valid @RequestBody InvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.addInvoice(request));
    }
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }
    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(invoiceService.getInvoicesByOrder(orderId));
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<InvoiceResponse> updateInvoiceStatus(@PathVariable String id, @RequestParam Invoice.InvoiceStatus status) {
        return ResponseEntity.ok(invoiceService.updateInvoiceStatus(id, status));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

}
