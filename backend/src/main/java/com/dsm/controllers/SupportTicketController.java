package com.dsm.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dsm.dto.request.RequestDTO.PublicSupportTicketRequest;
import com.dsm.dto.request.RequestDTO.SupportTicketRequest;
import com.dsm.dto.request.RequestDTO.SupportTicketStatusRequest;
import com.dsm.dto.response.ResponseDTO.SupportTicketResponse;
import com.dsm.services.SupportTicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/support/tickets")
@RequiredArgsConstructor
@Tag(name = "Support", description = "Support Ticket Management")
public class SupportTicketController {
    private final SupportTicketService supportTicketService;

    @PostMapping("/public")
    @Operation(summary = "Create Public Support Ticket")
    public ResponseEntity<SupportTicketResponse> createPublicTicket(@Valid @RequestBody PublicSupportTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supportTicketService.createPublicTicket(request));
    }
    @GetMapping
    @PreAuthorize("hasRole('administrator')")
    @Operation(summary = "Find All Support Tickets")
    public ResponseEntity<List<SupportTicketResponse>> getTickets(@RequestParam(required = false) String criteria) {
        return ResponseEntity.ok(supportTicketService.getTickets(criteria));
    }
    @PostMapping
    @PreAuthorize("hasRole('administrator')")
    @Operation(summary = "Create Support Ticket")
    public ResponseEntity<SupportTicketResponse> createTicket(@Valid @RequestBody SupportTicketRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supportTicketService.createTicket(request, authentication.getName()));
    }
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('administrator')")
    @Operation(summary = "Update Support Ticket")
    public ResponseEntity<SupportTicketResponse> updateTicket(@PathVariable String id, @Valid @RequestBody SupportTicketStatusRequest request) {
        return ResponseEntity.ok(supportTicketService.updateTicket(id, request));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('administrator')")
    @Operation(summary = "Delete Support Ticket")
    public ResponseEntity<Void> deleteTicket(@PathVariable String id) {
        supportTicketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
