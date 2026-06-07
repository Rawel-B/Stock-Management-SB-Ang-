package com.dsm.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dsm.dto.request.RequestDTO.CustomerRequest;
import com.dsm.dto.response.ResponseDTO.CustomerResponse;
import com.dsm.services.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer Management")
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create New Customer")
    public ResponseEntity<CustomerResponse> createNewCustomer(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.addCustomer(request));
    }
    @GetMapping("/{id}")
    @Operation(summary = "Find Customer By ID")
    public ResponseEntity<CustomerResponse> getCustomerByCustomerId(@PathVariable String id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }
    @GetMapping
    @Operation(summary = "Find All Customers")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers(@RequestParam(required = false) String criteria) {
        if (criteria != null && !criteria.isBlank()) {
            return ResponseEntity.ok(customerService.getCustomerByName(criteria));
        }

        return ResponseEntity.ok(customerService.getAllCustomers());
    }
    @PutMapping("/{id}")
    @Operation(summary = "Update a Customer")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable String id, @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Customer")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
