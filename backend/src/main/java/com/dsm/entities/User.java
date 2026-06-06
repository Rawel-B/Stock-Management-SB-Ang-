package com.dsm.entities;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private String id;
    private String username;
    private String password;
    private String name;
    private String email;
    @Builder.Default
    private Role role = Role.user;
    @Builder.Default
    private Boolean isActive = true;
    //====> TimeStamps
    @CreatedDate
    private LocalDateTime createdAt;    
    //====>

    public enum Role { administrator, manager, user }
}