package com.dsm.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.dsm.dto.request.RequestDTO.ShippingRequest;
import com.dsm.dto.response.ResponseDTO.ShippingResponse;
import com.dsm.entities.Shipping;
import com.dsm.services.ShippingService;

@RestController
@RequestMapping("/api/shippings")
@RequiredArgsConstructor
@Tag(name = "Shippings", description = "Shipping Management")
public class ShippingController {
    private final ShippingService shippingService;

    @PostMapping
    public ResponseEntity<ShippingResponse> createNewShipping(@Valid @RequestBody ShippingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shippingService.addShipping(request));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ShippingResponse> getShippingById(@PathVariable String id) {
        return ResponseEntity.ok(shippingService.getShippingById(id));
    }
    @GetMapping
    public ResponseEntity<List<ShippingResponse>> getShippingsByStatus(@RequestParam(required = false) Shipping.ShippingStatus status) {
        if (status != null) return ResponseEntity.ok(shippingService.getShippingsByStatus(status));
        return ResponseEntity.ok(shippingService.getAllShippings());
    }
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ShippingResponse>> getShippingsByOrder(@PathVariable String commandeId) {
        return ResponseEntity.ok(shippingService.getShippingsByOrder(commandeId));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ShippingResponse> updateShipping(@PathVariable String id, @Valid @RequestBody ShippingRequest request) {
        return ResponseEntity.ok(shippingService.updateShipping(id, request));
    }
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update a Shipping's Status")
    public ResponseEntity<ShippingResponse> setShippingStatus(@PathVariable String id, @RequestParam Shipping.ShippingStatus statut) {
        return ResponseEntity.ok(shippingService.updateShippingStatus(id, statut));
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Shipping")
    public ResponseEntity<Void> deleteShipping(@PathVariable String id) {
        shippingService.deleteShipping(id);
        return ResponseEntity.noContent().build();
    }
    
}
