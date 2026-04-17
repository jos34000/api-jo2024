package dev.jos.back.controller;

import dev.jos.back.dto.ticket.ScanRequestDTO;
import dev.jos.back.dto.ticket.ScanResponseDTO;
import dev.jos.back.service.TicketValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Ticket Validation", description = "Scanning and validation of tickets at event entrances")
public class TicketValidationController {

    private final TicketValidationService ticketValidationService;

    /**
     * Scans a ticket QR code (combinedKey) and marks it as used. Restricted to STAFF and ADMIN roles.
     * Returns ALREADY_USED outcome (without error) if the ticket was already scanned.
     */
    @PostMapping("/scan")
    @Operation(summary = "Scan a ticket", description = "Validates and marks a ticket as scanned via its combinedKey (QR code content). Restricted to STAFF and ADMIN.")
    public ResponseEntity<ScanResponseDTO> scan(
            Authentication authentication,
            @Valid @RequestBody ScanRequestDTO request) {
        String agentEmail = authentication.getName();
        return ResponseEntity.ok(ticketValidationService.scan(request.combinedKey(), agentEmail));
    }
}
