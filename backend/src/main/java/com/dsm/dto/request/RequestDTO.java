package com.dsm.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.dsm.entities.Invoice;
import com.dsm.entities.SupportTicket;
import com.dsm.entities.User;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
        private String locationId;
        @Min(0) private Integer quantity;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LocationRequest {
        @NotBlank(message = "name must be filled.")
        private String name;
        private String code;
        private String description;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderRequest {
        @NotNull(message = "the customer must be specified.")
        private String customerId;
        @NotBlank(message = "the supplier must be specified.")
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
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SignInRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ForgotPasswordRequest {
        @Email @NotBlank @Size(max = 120) @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$") private String email;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SignUpRequest {
        @NotBlank @Size(min = 3, max = 30) @Pattern(regexp = "^[a-zA-Z0-9._-]+$") private String username;
        @NotBlank @Size(min = 6, max = 72) private String password;
        @NotBlank @Size(min = 2, max = 80) private String name;
        @Email @NotBlank @Size(max = 120) @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$") private String email;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProfileRequest {
        @NotBlank @Size(min = 3, max = 30) @Pattern(regexp = "^[a-zA-Z0-9._-]+$") private String username;
        @NotBlank @Size(min = 2, max = 80) private String name;
        @Email @NotBlank @Size(max = 120) @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$") private String email;
        @Size(min = 6, max = 72) private String password;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserRequest {
        @NotBlank @Size(min = 3, max = 30) @Pattern(regexp = "^[a-zA-Z0-9._-]+$") private String username;
        @NotBlank @Size(min = 2, max = 80) private String name;
        @Email @NotBlank @Size(max = 120) @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$") private String email;
        @NotNull private User.Role role;
        private Boolean isActive;
        @Size(min = 6, max = 72) private String password;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SupportTicketRequest {
        @NotBlank @Size(min = 3, max = 120) private String subject;
        @NotBlank @Size(min = 10, max = 2000) private String description;
        @NotNull private SupportTicket.Category category;
        @NotNull private SupportTicket.Priority priority;
        private String assignedUserId;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SupportTicketStatusRequest {
        @NotNull private SupportTicket.Status status;
        private String assignedUserId;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PublicSupportTicketRequest {
        @NotBlank @Size(min = 3, max = 120) private String subject;
        @NotBlank @Size(min = 10, max = 2000) private String description;
        @Email @Size(max = 120) @Pattern(regexp = "^$|^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$") private String email;
        @NotNull private SupportTicket.Category category;
    }
}
