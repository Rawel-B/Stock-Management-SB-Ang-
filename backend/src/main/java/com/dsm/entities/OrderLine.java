package com.dsm.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "orderlines")
public class OrderLine {
    @Id
    private String id;
    @NotNull(message = "the order must be specified.")
    private String orderId;
    @NotBlank(message = "the product is mandatory.")
    private String product;
    private String productRef;
    @Min(value = 1, message = "cannot enter a quantity less than 1.")
    private Integer quantity;
    @Positive(message = "price per unit cannot be less than zero.")
    private BigDecimal pricePerUnit;
    private BigDecimal subTotal;
    //====> TimeStamps
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    //====>

    public void calculateSubTotal() {
        if (quantity != null && pricePerUnit != null) {
            this.subTotal = pricePerUnit.multiply(BigDecimal.valueOf(quantity));
        }
    }

}