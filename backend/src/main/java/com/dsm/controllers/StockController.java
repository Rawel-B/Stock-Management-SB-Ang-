package com.dsm.controllers;

import com.dsm.dto.request.RequestDTO.StockRequest;
import com.dsm.dto.response.ResponseDTO.StockResponse;
import com.dsm.services.StockService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Tag(name = "Stocks", description = "Stock Management")
public class StockController {
    private final StockService stockService;

    @PostMapping
    public ResponseEntity<StockResponse> createNewStock(@Valid @RequestBody StockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.addStock(request));
    }
    @GetMapping("/{id}")
    public ResponseEntity<StockResponse> getStockById(@PathVariable String id) {
        return ResponseEntity.ok(stockService.getStockById(id));
    }
    @GetMapping
    public ResponseEntity<List<StockResponse>> getAllStocks(@RequestParam(required = false) String criteria) {
        if (criteria != null && !criteria.isBlank()) return ResponseEntity.ok(stockService.getStockByProduct(criteria));
        return ResponseEntity.ok(stockService.getAllStocks());
    }
    @PutMapping("/{id}")
    public ResponseEntity<StockResponse> updateStock(@PathVariable String id, @Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(stockService.updateStock(id, request));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable String id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }

}
