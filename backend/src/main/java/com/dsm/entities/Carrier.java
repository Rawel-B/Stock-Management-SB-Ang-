package com.dsm.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Document(collection = "carriers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrier {
    @Id
    private String id;
    @NotBlank(message = "name must be filled.")
    private String name;
    private String phone;
    @DecimalMin(value = "0.0", message = "rating minimum is 0")
    @DecimalMax(value = "5.0", message = "rating maximum is 5")
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;
    @Builder.Default
    private Boolean isActive = true;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<String> shippingIds = new ArrayList<>();
    //====> TimeStamps
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    //====> 
}
