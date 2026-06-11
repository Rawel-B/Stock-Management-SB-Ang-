package com.dsm.entities;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "support_tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket {
    @Id
    private String id;
    private String subject;
    private String description;
    @Builder.Default
    private Category category = Category.operations;
    @Builder.Default
    private Priority priority = Priority.normal;
    @Builder.Default
    private Status status = Status.open;
    private String requesterId;
    private String requesterName;
    private String requesterEmail;
    private String assignedUserId;
    private String assignedUserName;
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Category { operations, account, data, technical, access, accountActivation }
    public enum Priority { low, normal, high, urgent }
    public enum Status { open, inProgress, resolved, closed }
}
