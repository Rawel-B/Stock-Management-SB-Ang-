package com.dsm.services;

import com.dsm.dto.request.RequestDTO.OrderRequest;
import com.dsm.dto.request.RequestDTO.ProductRequest;
import com.dsm.dto.response.ResponseDTO.CustomerResponse;
import com.dsm.dto.response.ResponseDTO.InvoiceResponse;
import com.dsm.dto.response.ResponseDTO.OrderResponse;
import com.dsm.dto.response.ResponseDTO.OrderSummaryResponse;
import com.dsm.dto.response.ResponseDTO.ProductResponse;
import com.dsm.dto.response.ResponseDTO.ShippingResponse;
import com.dsm.dto.response.ResponseDTO.SupplierResponse;
import com.dsm.entities.*;
import com.dsm.exception.*;
import com.dsm.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ShippingRepository shippingRepository;
    private final InvoiceRepository invoiceRepository;
    private final SupplierRepository supplierRepository;
    private final StockRepository stockRepository;
    private final CustomerService customerService;

    public OrderResponse addOrder(OrderRequest request) {
        Customer customer = customerService.findById(request.getCustomerId());
        Supplier supplier = findUsableSupplier(request.getSupplierId());
        validateProductsFromStock(request.getProducts());
        Order order = Order.builder()
                .customerId(customer.getId())
                .supplierId(supplier != null ? supplier.getId() : null)
                .orderDate(LocalDateTime.now())
                .status(Order.OrderStatus.pendingApproval)
                .remark(request.getRemark())
                .build();
        order.init();
        order = orderRepository.save(order);
        BigDecimal total = BigDecimal.ZERO;

        for (ProductRequest products : request.getProducts()) {
            Product product = Product.builder()
                    .orderId(order.getId())
                    .product(products.getProduct())
                    .productRef(products.getProductRef())
                    .quantity(products.getQuantity())
                    .pricePerUnit(products.getPricePerUnit())
                    .build();
            product.calculateSubTotal();
            productRepository.save(product);
            total = total.add(product.getSubTotal());
        }

        order.setTotalAmount(total);
        return toResponse(orderRepository.save(order));
    }
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String id) {
        return toResponse(findById(id));
    }
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toSummary);
    }
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getOrdersByCustomer(String customerId) {
        return orderRepository.getOrdersByCustomerId(customerId).stream().map(this::toSummary).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getOrdersBySupplier(String supplierId) {
        return orderRepository.getOrdersBySupplierId(supplierId).stream().map(this::toSummary).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getOrderByStatus(Order.OrderStatus status) {
        return orderRepository.getOrdersByStatus(status).stream().map(this::toSummary).collect(Collectors.toList());
    }
    public OrderResponse validateOrder(String id) {
        Order order = findById(id);

        if (order.getStatus() != Order.OrderStatus.pendingApproval) {
            throw new BusinessException("Only Pending Approval Orders Can Be Validated.");
        }

        order.setStatus(Order.OrderStatus.validated);
        return toResponse(orderRepository.save(order));
    }
    public OrderResponse updateStatut(String id, Order.OrderStatus status) {
        Order order = findById(id);

        if (order.getStatus() == status) {
            return toResponse(order);
        }

        if (order.getStatus() == Order.OrderStatus.pendingApproval) {
            if (status == Order.OrderStatus.validated || status == Order.OrderStatus.cancelled) {
                order.setStatus(status);
                return toResponse(orderRepository.save(order));
            }
            throw new BusinessException("Pending Approval Orders Must Be Validated Before Moving Forward.");
        }

        if (order.getStatus() == Order.OrderStatus.validated) {
            if (status == Order.OrderStatus.cancelled) {
                order.setStatus(status);
                return toResponse(orderRepository.save(order));
            }
            throw new BusinessException("Create A Delivery To Move This Order Forward.");
        }

        if (order.getStatus() == Order.OrderStatus.ongoing) {
            throw new BusinessException("Update The Related Delivery To Complete This Order.");
        }

        if (order.getStatus() == Order.OrderStatus.delivered || order.getStatus() == Order.OrderStatus.cancelled) {
            throw new BusinessException("This Order Is Already Closed.");
        }

        order.setStatus(status);
        return toResponse(orderRepository.save(order));
    }
    public OrderResponse update(String id, OrderRequest request) {
        Order order = findById(id);

        if (order.getStatus() != Order.OrderStatus.pendingApproval && order.getStatus() != Order.OrderStatus.validated) {
            throw new BusinessException("Only Pending Approval Or Validated Orders Can Be Modified.");
        }

        Customer customer = customerService.findById(request.getCustomerId());
        Supplier supplier = findUsableSupplier(request.getSupplierId());
        validateProductsFromStock(request.getProducts());
        order.setCustomerId(customer.getId());
        order.setSupplierId(supplier != null ? supplier.getId() : null);
        order.setRemark(request.getRemark());
        productRepository.deleteProductByOrderId(id);
        BigDecimal total = BigDecimal.ZERO;

        for (ProductRequest products : request.getProducts()) {
            Product product = Product.builder()
                    .orderId(order.getId())
                    .product(products.getProduct())
                    .productRef(products.getProductRef())
                    .quantity(products.getQuantity())
                    .pricePerUnit(products.getPricePerUnit())
                    .build();
            product.calculateSubTotal();
            productRepository.save(product);
            total = total.add(product.getSubTotal());
        }

        order.setTotalAmount(total);
        return toResponse(orderRepository.save(order));
    }
    public void deleteOrder(String id) {
        Order order = findById(id);

        if (order.getStatus() != Order.OrderStatus.pendingApproval && order.getStatus() != Order.OrderStatus.cancelled) {
            throw new BusinessException("You May Only Remove Orders That Are Pending Approval Or Cancelled.");
        }

        productRepository.deleteProductByOrderId(id);
        orderRepository.deleteById(id);
    }

    public Order findById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order With " + id + " Was Not Found."));
    }
    private Customer findCustomer(String customerId) {
        if (customerId == null) {
            return null;
        }
        try {
            return customerService.findById(customerId);
        } catch (ResourceNotFoundException ex) {
            return null;
        }
    }
    private Supplier findSupplier(String supplierId) {
        if (supplierId == null || supplierId.isBlank()) {
            return null;
        }
        return supplierRepository.findById(supplierId).orElseThrow(() -> new ResourceNotFoundException("Supplier With " + supplierId + " Was Not Found."));
    }
    private Supplier findUsableSupplier(String supplierId) {
        if (supplierId == null || supplierId.isBlank()) {
            throw new BusinessException("Supplier Must Be Specified.");
        }
        Supplier supplier = findSupplier(supplierId);
        if (supplier != null && !Boolean.TRUE.equals(supplier.getIsActive())) {
            throw new BusinessException("Inactive Suppliers Cannot Be Assigned To Orders.");
        }
        return supplier;
    }
    private void validateProductsFromStock(List<ProductRequest> products) {
        if (products == null || products.isEmpty()) {
            throw new BusinessException("At Least One Product Must Be Added.");
        }
        List<Stock> stocks = stockRepository.findAll();
        Map<String, Integer> available = stocks.stream()
                .filter(stock -> stock.getProduct() != null && !stock.getProduct().isBlank())
                .collect(Collectors.groupingBy(stock -> stock.getProduct().trim().toLowerCase(), Collectors.summingInt(stock -> stock.getQuantity() != null ? stock.getQuantity() : 0)));
        Map<String, Integer> requested = products.stream()
                .filter(product -> product.getProduct() != null && !product.getProduct().isBlank())
                .collect(Collectors.groupingBy(product -> product.getProduct().trim().toLowerCase(), Collectors.summingInt(product -> product.getQuantity() != null ? product.getQuantity() : 0)));

        for (Map.Entry<String, Integer> product : requested.entrySet()) {
            int quantity = available.getOrDefault(product.getKey(), 0);
            if (quantity <= 0) {
                throw new BusinessException("Product Must Exist In Stock.");
            }
            if (product.getValue() > quantity) {
                throw new BusinessException("Requested Quantity Exceeds Available Stock.");
            }
        }
    }
    private OrderResponse toResponse(Order order) {
        List<Product> orderProducts = productRepository.getProductsByOrderId(order.getId());
        List<Shipping> orderShippings = shippingRepository.getShippingsByOrderId(order.getId());
        List<Invoice> orderInvoices = invoiceRepository.getInvoicesByOrderId(order.getId());
        Customer customer = findCustomer(order.getCustomerId());
        Supplier supplier = findSupplier(order.getSupplierId());
        CustomerResponse customerResponse = null;
        SupplierResponse supplierResponse = null;

        List<ProductResponse> products = orderProducts.stream().map(l -> ProductResponse.builder()
                .id(l.getId()).product(l.getProduct())
                .productRef(l.getProductRef())
                .quantity(l.getQuantity())
                .pricePerUnit(l.getPricePerUnit())
                .subTotal(l.getSubTotal())
                .build()).collect(Collectors.toList());
        List<ShippingResponse> shippings = orderShippings.stream().map(shipping -> ShippingResponse.builder()
                .id(shipping.getId())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(shipping.getStatus())
                .trackingNumber(shipping.getTrackingNumber())
                .deliveryDate(shipping.getDeliveryDate())
                .receiptDate(shipping.getReceiptDate())
                .cost(shipping.getCost())
                .shippingAddress(shipping.getShippingAddress())
                .remark(shipping.getRemark())
                .build())
                .collect(Collectors.toList());
        List<InvoiceResponse> invoices = orderInvoices.stream().map(invoice -> InvoiceResponse.builder()
                .id(invoice.getId())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .invoiceStatus(invoice.getStatus())
                .invoicingMethod(invoice.getMethod())
                .amount(invoice.getAmount())
                .invoicingDate(invoice.getInvoicingDate())
                .transactionRef(invoice.getTransactionRef())
                .remark(invoice.getRemark())
                .createdAt(invoice.getCreatedAt())
                .build())
                .collect(Collectors.toList());

        if (customer != null) {
            customerResponse = CustomerResponse.builder()
                    .id(customer.getId())
                    .name(customer.getName())
                    .email(customer.getEmail())
                    .phone(customer.getPhone())
                    .build();
        }
        if (supplier != null) {
            supplierResponse = SupplierResponse.builder()
                    .id(supplier.getId())
                    .name(supplier.getName())
                    .email(supplier.getEmail())
                    .phone(supplier.getPhone())
                    .address(supplier.getAddress())
                    .isActive(supplier.getIsActive())
                    .ordersCount(orderRepository.getOrdersBySupplierId(supplier.getId()).size())
                    .createdAt(supplier.getCreatedAt())
                    .build();
        }

        return OrderResponse.builder()
                .id(order.getId()).orderNumber(order.getOrderNumber())
                .customer(customerResponse).orderDate(order.getOrderDate())
                .supplier(supplierResponse)
                .status(order.getStatus()).totalAmount(order.getTotalAmount())
                .remark(order.getRemark()).products(products)
                .shippings(shippings).invoices(invoices)
                .createdAt(order.getCreatedAt()).updatedAt(order.getUpdatedAt())
                .build();
    }
    private OrderSummaryResponse toSummary(Order order) {
        Customer customer = findCustomer(order.getCustomerId());
        Supplier supplier = findSupplier(order.getSupplierId());
        return OrderSummaryResponse.builder()
                .id(order.getId()).orderNumber(order.getOrderNumber())
                .customerName(customer != null ? customer.getName() : "N/A")
                .supplierName(supplier != null ? supplier.getName() : "N/A")
                .orderDate(order.getOrderDate()).status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .ordersCount(productRepository.getProductsByOrderId(order.getId()).size())
                .build();
    }
}
