package com.dsm.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Document(collection = "shippings")
public class Shipping {
    @Id
    private String id;
    @NotNull(message = "the order must be specified.")
    private String orderId;
    private String carrierId;
    private LocalDateTime deliveryDate; // Planned
    private LocalDateTime receiptDate; // Actual
    @Builder.Default
    private BigDecimal cost = BigDecimal.ZERO;
    @Builder.Default
    private ShippingStatus status = ShippingStatus.inPerparation; // Preparation As Default
    private String shippingAddress;
    private String trackingNumber;
    private String remark;
    //====> TimeStamps
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    //====>
    
    public enum ShippingStatus { inPerparation, shipped, inTransit, delivered, failed, returned }
    
}
