package com.carmeet.ms_live_scoreboard.controller;

import com.carmeet.ms_live_scoreboard.dto.ApiResponse;
import com.carmeet.ms_live_scoreboard.dto.PuntuacionDTO;
import com.carmeet.ms_live_scoreboard.model.Puntuacion;
import com.carmeet.ms_live_scoreboard.service.PuntuacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scoreboard")
@RequiredArgsConstructor
public class PuntuacionController {

    private final PuntuacionService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Puntuacion>> puntuar(@Valid @RequestBody PuntuacionDTO dto) {
        return ResponseEntity.ok(ApiResponse.<Puntuacion>builder()
                .success(true)
                .data(service.registrarPuntos(dto))
                .build());
    }
}
