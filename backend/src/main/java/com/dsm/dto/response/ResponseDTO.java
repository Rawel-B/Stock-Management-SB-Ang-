package com.dsm.dto.response;

import com.dsm.entities.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ResponseDTO {
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CustomerResponse {
        private String id;
        private String name;
        private String email;
        private String address;
        private String phone;
        private int ordersCount;
        private LocalDateTime createdAt;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CarrierResponse {
        private String id;
        private String name;
        private String phone;
        private BigDecimal rating;
        private Boolean isActive;
        private int shippingsCount;
        private LocalDateTime createdAt;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SupplierResponse {
        private String id;
        private String name;
        private String email;
        private String phone;
        private String address;
        private Boolean isActive;
        private int ordersCount;
        private LocalDateTime createdAt;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProductResponse {
        private String id;
        private String product;
        private String productRef;
        private Integer quantity;
        private BigDecimal pricePerUnit;
        private BigDecimal subTotal;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StockResponse {
        private String id;
        private String product;
        private String productRef;
        private Integer quantity;
        private LocalDateTime lastReceiptDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderResponse {
        private String id;
        private String orderNumber;
        private CustomerResponse customer;
        private SupplierResponse supplier;
        private LocalDateTime orderDate;
        private Order.OrderStatus status;
        private BigDecimal totalAmount;
        private String remark;
        private List<ProductResponse> products;
        private List<ShippingResponse> shippings;
        private List<InvoiceResponse> invoices;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderSummaryResponse {
        private String id;
        private String orderNumber;
        private String customerName;
        private String supplierName;
        private LocalDateTime orderDate;
        private Order.OrderStatus status;
        private BigDecimal totalAmount;
        private int ordersCount;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ShippingResponse {
        private String id;
        private String orderId;
        private String orderNumber;
        private CarrierResponse carrier;
        private LocalDateTime deliveryDate;
        private LocalDateTime receiptDate;
        private BigDecimal cost;
        private Shipping.ShippingStatus status;
        private String shippingAddress;
        private String trackingNumber;
        private String remark;
        private LocalDateTime createdAt;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class InvoiceResponse {
        private String id;
        private String orderId;
        private String orderNumber;
        private LocalDateTime invoicingDate;
        private Invoice.InvoiceStatus invoiceStatus;
        private Invoice.InvoicingMethod invoicingMethod;
        private BigDecimal amount;
        private String transactionRef;
        private String remark;
        private LocalDateTime createdAt;
    }
    //====> Authentication
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String username;
        private String name;
        private String role;
    }
    //====>

}
