package com.dsm.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dsm.dto.request.RequestDTO.PublicSupportTicketRequest;
import com.dsm.dto.request.RequestDTO.SupportTicketRequest;
import com.dsm.dto.request.RequestDTO.SupportTicketStatusRequest;
import com.dsm.dto.response.ResponseDTO.SupportTicketResponse;
import com.dsm.entities.SupportTicket;
import com.dsm.entities.User;
import com.dsm.exception.ResourceNotFoundException;
import com.dsm.repositories.SupportTicketRepository;
import com.dsm.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupportTicketService {
    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;

    public List<SupportTicketResponse> getTickets(String criteria) {
        List<SupportTicket> tickets = criteria != null && !criteria.isBlank()
                ? supportTicketRepository.findByCriteria(criteria.trim())
                : supportTicketRepository.findAll();

        return tickets.stream().map(this::toResponse).collect(Collectors.toList());
    }
    public SupportTicketResponse createTicket(SupportTicketRequest request, String username) {
        User requester = userRepository.getUserByUsername(username).orElseThrow(() -> new ResourceNotFoundException("No User Found."));
        SupportTicket ticket = SupportTicket.builder()
                .subject(request.getSubject().trim())
                .description(request.getDescription().trim())
                .category(request.getCategory())
                .priority(request.getPriority())
                .requesterId(requester.getId())
                .requesterName(requester.getName())
                .requesterEmail(requester.getEmail())
                .build();
        assignTicket(ticket, request.getAssignedUserId());

        return toResponse(supportTicketRepository.save(ticket));
    }
    public SupportTicketResponse createPublicTicket(PublicSupportTicketRequest request) {
        SupportTicket ticket = SupportTicket.builder()
                .subject(request.getSubject().trim())
                .description(request.getDescription().trim())
                .category(request.getCategory())
                .priority(SupportTicket.Priority.high)
                .requesterEmail(request.getEmail() == null ? null : request.getEmail().trim().toLowerCase())
                .requesterName("Access request")
                .build();

        return toResponse(supportTicketRepository.save(ticket));
    }
    public SupportTicketResponse createAccountActivationTicket(User user) {
        SupportTicket ticket = SupportTicket.builder()
                .subject("Account Activation")
                .description("Activate user account: " + user.getUsername())
                .category(SupportTicket.Category.accountActivation)
                .priority(SupportTicket.Priority.high)
                .requesterId(user.getId())
                .requesterName(user.getName())
                .requesterEmail(user.getEmail())
                .build();

        return toResponse(supportTicketRepository.save(ticket));
    }
    public SupportTicketResponse updateTicket(String id, SupportTicketStatusRequest request) {
        SupportTicket ticket = findById(id);
        ticket.setStatus(request.getStatus());
        ticket.setUpdatedAt(LocalDateTime.now());
        assignTicket(ticket, request.getAssignedUserId());

        return toResponse(supportTicketRepository.save(ticket));
    }
    public void deleteTicket(String id) {
        findById(id);
        supportTicketRepository.deleteById(id);
    }
    private SupportTicket findById(String id) {
        return supportTicketRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ticket With " + id + " Was Not Found."));
    }
    private void assignTicket(SupportTicket ticket, String assignedUserId) {
        if (assignedUserId == null || assignedUserId.isBlank()) {
            ticket.setAssignedUserId(null);
            ticket.setAssignedUserName(null);
            return;
        }
        User assignedUser = userRepository.findById(assignedUserId).orElseThrow(() -> new ResourceNotFoundException("Assigned User Was Not Found."));
        ticket.setAssignedUserId(assignedUser.getId());
        ticket.setAssignedUserName(assignedUser.getName());
    }
    private SupportTicketResponse toResponse(SupportTicket ticket) {
        return SupportTicketResponse.builder()
                .id(ticket.getId())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .category(ticket.getCategory())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .requesterId(ticket.getRequesterId())
                .requesterName(ticket.getRequesterName())
                .requesterEmail(ticket.getRequesterEmail())
                .assignedUserId(ticket.getAssignedUserId())
                .assignedUserName(ticket.getAssignedUserName())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
