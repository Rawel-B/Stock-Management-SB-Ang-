package com.dsm.services;

import com.dsm.dto.request.RequestDTO.StockRequest;
import com.dsm.dto.response.ResponseDTO.StockResponse;
import com.dsm.entities.Location;
import com.dsm.entities.Order;
import com.dsm.entities.Product;
import com.dsm.entities.Stock;
import com.dsm.exception.BusinessException;
import com.dsm.exception.DuplicateResourceException;
import com.dsm.exception.ResourceNotFoundException;
import com.dsm.repositories.LocationRepository;
import com.dsm.repositories.OrderRepository;
import com.dsm.repositories.ProductRepository;
import com.dsm.repositories.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StockService {
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final OrderRepository orderRepository;

    public StockResponse addStock(StockRequest request) {
        if (request.getProductRef() != null && !request.getProductRef().isBlank() && stockRepository.getStockByProductRef(request.getProductRef()).isPresent()) {
            throw new DuplicateResourceException("Stock With This Product Reference Already Exists.");
        }

        Location location = findLocation(request.getLocationId());
        Stock stock = Stock.builder()
                .product(request.getProduct())
                .productRef(request.getProductRef())
                .locationId(location != null ? location.getId() : null)
                .location(location != null ? location.getName() : null)
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
        List<Stock> stocks = stockRepository.findAll();
        Map<String, Integer> reserved = reservedProducts();
        Map<String, Integer> totals = stockTotals(stocks);
        return stocks.stream().map(stock -> toResponse(stock, reserved, totals)).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<StockResponse> getStockByProduct(String product) {
        List<Stock> stocks = stockRepository.getStockByProduct(product);
        Map<String, Integer> reserved = reservedProducts();
        Map<String, Integer> totals = stockTotals(stockRepository.findAll());
        return stocks.stream().map(stock -> toResponse(stock, reserved, totals)).collect(Collectors.toList());
    }
    public StockResponse updateStock(String id, StockRequest request) {
        Stock stock = findById(id);

        if (request.getProductRef() != null && !request.getProductRef().isBlank() && stock.getProductRef() != null && !stock.getProductRef().equals(request.getProductRef()) && stockRepository.getStockByProductRef(request.getProductRef()).isPresent()) {
            throw new DuplicateResourceException("Stock With This Product Reference Already Exists.");
        }

        Location location = findLocation(request.getLocationId());
        validateStockChange(stock, request.getProduct(), request.getQuantity() != null ? request.getQuantity() : stock.getQuantity());
        stock.setProduct(request.getProduct());
        stock.setProductRef(request.getProductRef());
        stock.setLocationId(location != null ? location.getId() : null);
        stock.setLocation(location != null ? location.getName() : null);
        stock.setQuantity(request.getQuantity() != null ? request.getQuantity() : stock.getQuantity());
        return toResponse(stockRepository.save(stock));
    }
    public void deductOrder(Order order) {
        List<Product> products = productRepository.getProductsByOrderId(order.getId());
        Map<String, Integer> requested = products.stream()
                .filter(product -> product.getProduct() != null && !product.getProduct().isBlank())
                .collect(Collectors.groupingBy(product -> productKey(product.getProduct()), Collectors.summingInt(product -> product.getQuantity() != null ? product.getQuantity() : 0)));

        requested.forEach(this::deductProductFromStock);
        deleteEmptyStocks();
    }
    public void deleteStock(String id) {
        Stock stock = findById(id);
        validateStockRemoval(stock);
        stockRepository.deleteById(id);
    }
    private Stock findById(String id) {
        return stockRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Stock With ID " + id + " Was Not Found."));
    }
    private Location findLocation(String locationId) {
        if (locationId == null || locationId.isBlank()) {
            return null;
        }
        return locationRepository.findById(locationId).orElseThrow(() -> new ResourceNotFoundException("Location With ID " + locationId + " Was Not Found."));
    }
    private void deductProductFromStock(String product, Integer quantity) {
        int remaining = quantity;
        List<Stock> stocks = stockRepository.findAll().stream()
                .filter(stock -> productKey(stock.getProduct()).equals(product))
                .toList();
        int available = stocks.stream().mapToInt(stock -> stock.getQuantity() != null ? stock.getQuantity() : 0).sum();

        if (available < quantity) {
            throw new BusinessException("Requested Quantity Exceeds Available Stock.");
        }

        for (Stock stock : stocks) {
            if (remaining <= 0) {
                break;
            }
            int currentQuantity = stock.getQuantity() != null ? stock.getQuantity() : 0;
            int deductedQuantity = Math.min(currentQuantity, remaining);
            int updatedQuantity = currentQuantity - deductedQuantity;
            stock.setQuantity(updatedQuantity);
            if (updatedQuantity <= 0) {
                stockRepository.deleteById(stock.getId());
            } else {
                stockRepository.save(stock);
            }
            remaining -= deductedQuantity;
        }

        if (remaining > 0) {
            throw new BusinessException("Requested Quantity Exceeds Available Stock.");
        }
    }
    private void deleteEmptyStocks() {
        stockRepository.findAll().stream()
                .filter(stock -> stock.getQuantity() == null || stock.getQuantity() <= 0)
                .forEach(stock -> stockRepository.deleteById(stock.getId()));
    }
    private String productKey(String product) {
        return product != null ? product.trim().toLowerCase() : "";
    }
    private Map<String, Integer> reservedProducts() {
        return orderRepository.findAll().stream()
                .filter(this::reservesStock)
                .flatMap(order -> productRepository.getProductsByOrderId(order.getId()).stream())
                .filter(product -> product.getProduct() != null && !product.getProduct().isBlank())
                .collect(Collectors.groupingBy(product -> productKey(product.getProduct()), Collectors.summingInt(product -> product.getQuantity() != null ? product.getQuantity() : 0)));
    }
    private boolean reservesStock(Order order) {
        return order.getStatus() == Order.OrderStatus.pendingApproval || order.getStatus() == Order.OrderStatus.validated || order.getStatus() == Order.OrderStatus.ongoing;
    }
    private void validateStockChange(Stock stock, String product, Integer quantity) {
        Map<String, Integer> totals = stockTotals(stockRepository.findAll());
        String currentProduct = productKey(stock.getProduct());
        String nextProduct = productKey(product);
        int currentQuantity = stock.getQuantity() != null ? stock.getQuantity() : 0;
        int nextQuantity = quantity != null ? quantity : 0;
        totals.put(currentProduct, totals.getOrDefault(currentProduct, 0) - currentQuantity);
        totals.put(nextProduct, totals.getOrDefault(nextProduct, 0) + nextQuantity);
        validateReservedTotals(totals, currentProduct, nextProduct);
    }
    private void validateStockRemoval(Stock stock) {
        Map<String, Integer> totals = stockTotals(stockRepository.findAll());
        String product = productKey(stock.getProduct());
        int quantity = stock.getQuantity() != null ? stock.getQuantity() : 0;
        totals.put(product, totals.getOrDefault(product, 0) - quantity);
        validateReservedTotals(totals, product);
    }
    private void validateReservedTotals(Map<String, Integer> totals, String... products) {
        Map<String, Integer> reserved = reservedProducts();
        for (String product : products) {
            if (totals.getOrDefault(product, 0) < reserved.getOrDefault(product, 0)) {
                throw new BusinessException("Reserved Stock Cannot Be Reduced.");
            }
        }
    }
    private Map<String, Integer> stockTotals(List<Stock> stocks) {
        return stocks.stream()
                .filter(stock -> stock.getProduct() != null && !stock.getProduct().isBlank())
                .collect(Collectors.groupingBy(stock -> productKey(stock.getProduct()), Collectors.summingInt(stock -> stock.getQuantity() != null ? stock.getQuantity() : 0)));
    }
    private StockResponse toResponse(Stock stock) {
        return toResponse(stock, reservedProducts(), stockTotals(stockRepository.findAll()));
    }
    private StockResponse toResponse(Stock stock, Map<String, Integer> reservedProducts, Map<String, Integer> stockTotals) {
        String product = productKey(stock.getProduct());
        int reservedQuantity = reservedProducts.getOrDefault(product, 0);
        int availableQuantity = Math.max(stockTotals.getOrDefault(product, 0) - reservedQuantity, 0);

        return StockResponse.builder()
                .id(stock.getId())
                .product(stock.getProduct())
                .productRef(stock.getProductRef())
                .locationId(stock.getLocationId())
                .location(stock.getLocation())
                .quantity(stock.getQuantity())
                .reservedQuantity(reservedQuantity)
                .availableQuantity(availableQuantity)
                .lastReceiptDate(stock.getLastReceiptDate())
                .createdAt(stock.getCreatedAt())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }

}
