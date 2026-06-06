package com.dsm.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    @NotNull(message = "the customer must be specified.")
    private String customerId;
    private LocalDateTime orderDate;
    @Builder.Default
    private OrderStatus status = OrderStatus.pendingApproval;
    @Builder.Default
    private BigDecimal totalCost = BigDecimal.ZERO;
    private String orderNumber;
    private String remark;
    @Builder.Default
    private List<OrderLine> orderLines = new ArrayList<>();
    @Builder.Default
    private List<String> shippingIds = new ArrayList<>();
    @Builder.Default
    private List<String> invoiceIds = new ArrayList<>();
    //====> TimeStamps
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    //====>

    public enum OrderStatus { pendingApproval, validated, ongoing, delivered, cancelled }

    public void init() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (orderNumber == null) {
            orderNumber = "CMD-" + System.currentTimeMillis();
        }
    }

}