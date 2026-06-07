package com.dsm.controllers;

import com.dsm.dto.request.RequestDTO.CarrierRequest;
import com.dsm.dto.response.ResponseDTO.CarrierResponse;
import com.dsm.services.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/carriers")
@RequiredArgsConstructor
@Tag(name = "Carriers", description = "Carrier Management")
public class CarrierController {
    private final CarrierService carrierService;

    @PostMapping
    public ResponseEntity<CarrierResponse> createNewCarrier(@Valid @RequestBody CarrierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carrierService.addCarrier(request));
    }
    @GetMapping("/{id}")
    public ResponseEntity<CarrierResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(carrierService.getById(id));
    }
    @GetMapping
    public ResponseEntity<List<CarrierResponse>> getAll(@RequestParam(required = false) Boolean isActive) {
        if (Boolean.TRUE.equals(isActive)) return ResponseEntity.ok(carrierService.getActiveCarriers());
        return ResponseEntity.ok(carrierService.getAllCarriers());
    }
    @PutMapping("/{id}")
    public ResponseEntity<CarrierResponse> update(@PathVariable String id, @Valid @RequestBody CarrierRequest request) {
        return ResponseEntity.ok(carrierService.updateCarrier(id, request));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        carrierService.deleteCarrier(id);
        return ResponseEntity.noContent().build();
    }

}
