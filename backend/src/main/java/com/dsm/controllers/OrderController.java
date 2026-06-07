package com.dsm.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.dsm.dto.request.RequestDTO.OrderRequest;
import com.dsm.dto.response.ResponseDTO.OrderResponse;
import com.dsm.dto.response.ResponseDTO.OrderSummaryResponse;
import com.dsm.entities.Order;
import com.dsm.services.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order Management")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create New Customer")
    public ResponseEntity<OrderResponse> createNewOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.addOrder(request));
    }
    @GetMapping("/{id}")
    @Operation(summary = "Find Order By ID")
    public ResponseEntity<OrderResponse> findOrderById(@PathVariable String id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
    @GetMapping
    @Operation(summary = "Find All Orders")
    public ResponseEntity<Page<OrderSummaryResponse>> getAllOrders(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) Order.OrderStatus status) {
        if (status != null) {
            List<OrderSummaryResponse> list = orderService.getOrderByStatus(status);
            return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(list));
        }

        return ResponseEntity.ok(orderService.getAllOrders(PageRequest.of(page, size, Sort.by("orderDate").descending())));
    }
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Orders By Customer")
    public ResponseEntity<List<OrderSummaryResponse>> getOrdersByCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }
    @PutMapping("/{id}")
    @Operation(summary = "Update an Order")
    public ResponseEntity<OrderResponse> update(@PathVariable String id, @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.update(id, request));
    }
    @PatchMapping("/{id}/validateOrder")
    @Operation(summary = "Validate an Order")
    public ResponseEntity<OrderResponse> validateOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderService.validateOrder(id));
    }
    @PatchMapping("/{id}/setOrderStatus")
    @Operation(summary = "Set Order Status")
    public ResponseEntity<OrderResponse> setOrderStatus(@PathVariable String id, @RequestParam Order.OrderStatus statut) {
        return ResponseEntity.ok(orderService.updateStatut(id, statut));
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an Order")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

}
