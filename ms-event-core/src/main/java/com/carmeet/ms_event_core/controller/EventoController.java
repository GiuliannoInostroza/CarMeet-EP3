package com.carmeet.ms_event_core.controller;

import com.carmeet.ms_event_core.dto.ApiResponse;
import com.carmeet.ms_event_core.dto.EventoDTO;
import com.carmeet.ms_event_core.model.Evento;
import com.carmeet.ms_event_core.service.EventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Evento>> crear(@Valid @RequestBody EventoDTO dto) {
        return ResponseEntity.ok(ApiResponse.<Evento>builder()
                .success(true)
                .data(service.crear(dto))
                .build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<List<Evento>>> listar() {
        return ResponseEntity.ok(ApiResponse.<List<Evento>>builder()
                .success(true)
                .data(service.listar())
                .build());
    }
}
