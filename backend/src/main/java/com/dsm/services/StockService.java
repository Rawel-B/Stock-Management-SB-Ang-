package com.dsm.services;

import com.dsm.dto.request.RequestDTO.StockRequest;
import com.dsm.dto.response.ResponseDTO.StockResponse;
import com.dsm.entities.Order;
import com.dsm.entities.Product;
import com.dsm.entities.Stock;
import com.dsm.exception.DuplicateResourceException;
import com.dsm.exception.ResourceNotFoundException;
import com.dsm.repositories.ProductRepository;
import com.dsm.repositories.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StockService {
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;

    public StockResponse addStock(StockRequest request) {
        if (request.getProductRef() != null && !request.getProductRef().isBlank() && stockRepository.getStockByProductRef(request.getProductRef()).isPresent()) {
            throw new DuplicateResourceException("Stock With This Product Reference Already Exists.");
        }

        Stock stock = Stock.builder()
                .product(request.getProduct())
                .productRef(request.getProductRef())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 0)
                .build();
        return toResponse(stockRepository.save(stock));
    }
    @Transactional(readOnly = true)
    public StockResponse getStockById(String id) {
        return toResponse(findById(id));
    }
    @Transactional(readOnly = true)
    public List<StockResponse> getAllStocks() {
        return stockRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<StockResponse> getStockByProduct(String product) {
        return stockRepository.getStockByProduct(product).stream().map(this::toResponse).collect(Collectors.toList());
    }
    public StockResponse updateStock(String id, StockRequest request) {
        Stock stock = findById(id);

        if (request.getProductRef() != null && !request.getProductRef().isBlank() && stock.getProductRef() != null && !stock.getProductRef().equals(request.getProductRef()) && stockRepository.getStockByProductRef(request.getProductRef()).isPresent()) {
            throw new DuplicateResourceException("Stock With This Product Reference Already Exists.");
        }

        stock.setProduct(request.getProduct());
        stock.setProductRef(request.getProductRef());
        stock.setQuantity(request.getQuantity() != null ? request.getQuantity() : stock.getQuantity());
        return toResponse(stockRepository.save(stock));
    }
    public void receiveOrder(Order order) {
        List<Product> products = productRepository.getProductsByOrderId(order.getId());

        for (Product product : products) {
            Stock stock = findStockForProduct(product);
            int currentQuantity = stock.getQuantity() != null ? stock.getQuantity() : 0;
            int receivedQuantity = product.getQuantity() != null ? product.getQuantity() : 0;
            stock.setQuantity(currentQuantity + receivedQuantity);
            stock.setLastReceiptDate(LocalDateTime.now());
            stockRepository.save(stock);
        }
    }
    public void deleteStock(String id) {
        findById(id);
        stockRepository.deleteById(id);
    }
    private Stock findById(String id) {
        return stockRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Stock With ID " + id + " Was Not Found."));
    }
    private Stock findStockForProduct(Product product) {
        if (product.getProductRef() != null && !product.getProductRef().isBlank()) {
            return stockRepository.getStockByProductRef(product.getProductRef()).orElseGet(() -> Stock.builder()
                    .product(product.getProduct())
                    .productRef(product.getProductRef())
                    .quantity(0)
                    .build());
        }

        return stockRepository.getStockByProduct(product.getProduct()).stream().findFirst().orElseGet(() -> Stock.builder()
                .product(product.getProduct())
                .quantity(0)
                .build());
    }
    private StockResponse toResponse(Stock stock) {
        return StockResponse.builder()
                .id(stock.getId())
                .product(stock.getProduct())
                .productRef(stock.getProductRef())
                .quantity(stock.getQuantity())
                .lastReceiptDate(stock.getLastReceiptDate())
                .createdAt(stock.getCreatedAt())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }

}
