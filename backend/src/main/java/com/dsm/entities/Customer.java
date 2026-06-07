package com.dsm.entities;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "customers")
public class Customer {
    @Id
    private String id;
    @NotBlank
    private String name;
    @Email
    @NotBlank
    private String email;
    private String address;
    private String phone;
    //====> TimeStamps
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    //====>

    @java.lang.SuppressWarnings(value = "all")
    @lombok.Generated
    public static class CustomerBuilder {

        public Object nom(String tech_Solutions) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}