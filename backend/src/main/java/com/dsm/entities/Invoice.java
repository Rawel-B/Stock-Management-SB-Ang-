package com.dsm.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

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
@Document(collection = "invoices")
public class Invoice {
    @Id
    private String id;
    @NotNull(message = "the order must be specified.")
    private String orderId;
    private LocalDateTime invoicingDate;
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.pending;
    @NotNull(message = "must add an invoicing method.")
    private InvoicingMethod method;
    @Positive(message = "total amount cannot be negative.")
    private BigDecimal amount;
    private String transactionRef;
    private String remark;
    //====> TimeStamps
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    //====>

    public enum InvoiceStatus { pending, processing, completed, failed, refunded, cancelled }
    public enum InvoicingMethod { creditCard, debitCard, bankTransfer, Check, Cash, paypal, stripe, other }

}