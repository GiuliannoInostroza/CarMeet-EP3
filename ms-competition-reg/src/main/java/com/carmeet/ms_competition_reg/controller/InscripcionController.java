package com.carmeet.ms_competition_reg.controller;

import com.carmeet.ms_competition_reg.dto.ApiResponse;
import com.carmeet.ms_competition_reg.model.Inscripcion;
import com.carmeet.ms_competition_reg.model.Requisito;
import com.carmeet.ms_competition_reg.dto.InscripcionDTO;
import com.carmeet.ms_competition_reg.dto.RequisitoDTO;
import com.carmeet.ms_competition_reg.service.InscripcionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionService service;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<InscripcionDTO>>> listar() {
        List<InscripcionDTO> lista = service.listar().stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<InscripcionDTO>>builder().success(true).message("Listado").data(lista).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InscripcionDTO>> obtenerPorId(@PathVariable Long id) {
        InscripcionDTO dto = toDTO(service.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.<InscripcionDTO>builder().success(true).message("Encontrado").data(dto).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InscripcionDTO>> guardar(
            @Valid @RequestBody InscripcionDTO dto,
            HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        Inscripcion nuevo = service.guardar(toEntity(dto), bearer);
        return ResponseEntity.status(201).body(ApiResponse.<InscripcionDTO>builder()
                .success(true).message("Inscripción creada").data(toDTO(nuevo)).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InscripcionDTO>> actualizar(@PathVariable Long id, @Valid @RequestBody InscripcionDTO dto) {
        Inscripcion actualizado = service.actualizar(id, toEntity(dto));
        return ResponseEntity.ok(ApiResponse.<InscripcionDTO>builder().success(true).message("Actualizado").data(toDTO(actualizado)).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Eliminado").build());
    }

    // ── MÉTODOS DE NEGOCIO ────────────────────────────────────────────────────

    
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<ApiResponse<List<InscripcionDTO>>> porEvento(@PathVariable Long eventoId) {
        List<InscripcionDTO> lista = service.obtenerPorEventoId(eventoId).stream()
                .map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<InscripcionDTO>>builder()
                .success(true).message("Inscripciones del evento " + eventoId).data(lista).build());
    }

    
    @GetMapping("/vehiculo/{vehiculoId}")
    public ResponseEntity<ApiResponse<List<InscripcionDTO>>> porVehiculo(@PathVariable Long vehiculoId) {
        List<InscripcionDTO> lista = service.obtenerPorVehiculoId(vehiculoId).stream()
                .map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<InscripcionDTO>>builder()
                .success(true).message("Inscripciones del vehículo " + vehiculoId).data(lista).build());
    }

    
    @PatchMapping("/{id}/aprobar")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<InscripcionDTO>> aprobar(
            @PathVariable Long id, HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        InscripcionDTO dto = toDTO(service.aprobar(id, bearer));
        return ResponseEntity.ok(ApiResponse.<InscripcionDTO>builder()
                .success(true).message("Inscripción aprobada").data(dto).build());
    }

    
    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<InscripcionDTO>> rechazar(
            @PathVariable Long id, HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        InscripcionDTO dto = toDTO(service.rechazar(id, bearer));
        return ResponseEntity.ok(ApiResponse.<InscripcionDTO>builder()
                .success(true).message("Inscripción rechazada").data(dto).build());
    }

    // ── CONVERSIÓN ────────────────────────────────────────────────────────────

    private InscripcionDTO toDTO(Inscripcion e) {
        InscripcionDTO dto = new InscripcionDTO();
        dto.setId(e.getId());
        dto.setVehiculoId(e.getVehiculoId());
        dto.setEventoId(e.getEventoId());
        dto.setParticipante(e.getParticipante());
        dto.setCategoria(e.getCategoria());
        dto.setUsername(e.getUsername());
        dto.setEstado(e.getEstado());
        if (e.getRequisitos() != null) {
            dto.setRequisitos(e.getRequisitos().stream().map(r -> {
                RequisitoDTO rdto = new RequisitoDTO();
                rdto.setId(r.getId());
                rdto.setNombre(r.getNombre());
                rdto.setDescripcion(r.getDescripcion());
                return rdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Inscripcion toEntity(InscripcionDTO dto) {
        Inscripcion e = new Inscripcion();
        e.setVehiculoId(dto.getVehiculoId());
        e.setEventoId(dto.getEventoId());
        e.setParticipante(dto.getParticipante());
        e.setCategoria(dto.getCategoria());
        e.setUsername(dto.getUsername());
        e.setEstado(dto.getEstado() != null ? dto.getEstado() : "PENDIENTE");
        if (dto.getRequisitos() != null) {
            e.setRequisitos(dto.getRequisitos().stream().map(rdto -> {
                Requisito r = new Requisito();
                r.setNombre(rdto.getNombre());
                r.setDescripcion(rdto.getDescripcion());
                r.setInscripcion(e);
                return r;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
