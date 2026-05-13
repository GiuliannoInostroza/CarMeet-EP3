package com.carmeet.ms_ticketing.controller;

import com.carmeet.ms_ticketing.dto.ApiResponse;
import com.carmeet.ms_ticketing.dto.TicketDTO;
import com.carmeet.ms_ticketing.model.Ticket;
import com.carmeet.ms_ticketing.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService service;

    @PostMapping("/comprar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Ticket>> comprar(
            @Valid @RequestBody TicketDTO dto, 
            @RequestHeader("Authorization") String authHeader) {
        
        // Limpiamos el Bearer para pasar solo el token al service
        String token = authHeader.substring(7); 
        
        return ResponseEntity.ok(ApiResponse.<Ticket>builder()
                .success(true)
                .data(service.comprar(dto, token))
                .build());
    }
}
