package com.dsm.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.dsm.entities.Invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RequestDTO {
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CustomerRequest {
        @NotBlank(message = "name must be filled.")
        @Size(min = 2, max = 100)
        private String name;
        @Email(message = "invalid email format.")
        @NotBlank(message = "email must be filled.")
        private String email;
        private String address;
        private String phone;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CarrierRequest {
        @NotBlank(message = "name must be filled.")
        private String name;
        private String phone;
        @DecimalMin("0.0") @DecimalMax("5.0")
        private BigDecimal rating;
        private Boolean isActive;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SupplierRequest {
        @NotBlank(message = "name must be filled.")
        private String name;
        @Email(message = "invalid email format.")
        private String email;
        private String phone;
        private String address;
        private Boolean isActive;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProductRequest {
        @NotBlank(message = "the product must be specified.")
        private String product;
        private String productRef;
        @Min(1) private Integer quantity;
        @Positive private BigDecimal pricePerUnit;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StockRequest {
        @NotBlank(message = "the product must be specified.")
        private String product;
        private String productRef;
        @Min(0) private Integer quantity;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderRequest {
        @NotNull(message = "the customer must be specified.")
        private String customerId;
        private String supplierId;
        private String remark;
        @Valid
        @NotEmpty(message = "at least one product must be added.")
        private List<ProductRequest> products;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ShippingRequest {
        @NotNull private String orderId;
        private String carrierId;
        private LocalDateTime deliveryDate;
        private BigDecimal cost;
        private String shippingAddress;
        private String trackingNumber;
        private String remark;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class InvoiceRequest {
        @NotNull private String orderId;
        @NotNull private Invoice.InvoicingMethod method;
        @Positive @NotNull private BigDecimal amount;
        private String transactionRef;
        private String remark;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DashboardStats {
        private long ordersCount;
        private long ordersPendingApproval;
        private long ordersOngoing;
        private long ordersDelivered;
        private long ordersCancelled;
        private Double revenue;
        private long totalCustomers;
        private long totalCarriers;
        private long totalSuppliers;
        private long totalStocks;
        private long shippingInPerparation;
        private long invoicePending;
    }
    //====> Authentication
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SignInRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SignUpRequest {
        @NotBlank @Size(min = 3, max = 50) private String username;
        @NotBlank @Size(min = 6) private String password;
        @NotBlank private String name;
        @Email @NotBlank private String email;
    }
    //====>
}
