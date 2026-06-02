package com.carmeet.ms_venue_capacity.controller;

import com.carmeet.ms_venue_capacity.dto.ApiResponse;
import com.carmeet.ms_venue_capacity.model.Recinto;
import com.carmeet.ms_venue_capacity.model.Zona;
import com.carmeet.ms_venue_capacity.dto.RecintoDTO;
import com.carmeet.ms_venue_capacity.dto.ZonaDTO;
import com.carmeet.ms_venue_capacity.service.RecintoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/recintos")
@RequiredArgsConstructor
public class RecintoController {

    private final RecintoService service;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecintoDTO>>> listar() {
        List<RecintoDTO> lista = service.listar().stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<RecintoDTO>>builder().success(true).message("Listado").data(lista).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecintoDTO>> obtenerPorId(@PathVariable Long id) {
        RecintoDTO dto = toDTO(service.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.<RecintoDTO>builder().success(true).message("Encontrado").data(dto).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecintoDTO>> guardar(@Valid @RequestBody RecintoDTO dto) {
        Recinto nuevo = service.guardar(toEntity(dto));
        return ResponseEntity.status(201).body(ApiResponse.<RecintoDTO>builder().success(true).message("Creado").data(toDTO(nuevo)).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecintoDTO>> actualizar(@PathVariable Long id, @Valid @RequestBody RecintoDTO dto) {
        Recinto actualizado = service.actualizar(id, toEntity(dto));
        return ResponseEntity.ok(ApiResponse.<RecintoDTO>builder().success(true).message("Actualizado").data(toDTO(actualizado)).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Eliminado").build());
    }

    // ── MÉTODOS DE NEGOCIO ────────────────────────────────────────────────────

    
    @GetMapping("/{id}/zonas")
    public ResponseEntity<ApiResponse<List<ZonaDTO>>> listarZonas(@PathVariable Long id) {
        List<ZonaDTO> zonas = service.listarZonas(id).stream()
                .map(z -> {
                    ZonaDTO dto = new ZonaDTO();
                    dto.setId(z.getId());
                    dto.setNombre(z.getNombre());
                    return dto;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<ZonaDTO>>builder()
                .success(true).message("Zonas del recinto " + id).data(zonas).build());
    }

    
    @GetMapping("/{id}/disponibilidad")
    public ResponseEntity<ApiResponse<Map<String, Object>>> consultarDisponibilidad(@PathVariable Long id) {
        Map<String, Object> info = service.consultarDisponibilidad(id);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true).message("Disponibilidad del recinto").data(info).build());
    }

    
    @PostMapping("/{id}/registrar-ingreso")
    public ResponseEntity<ApiResponse<RecintoDTO>> registrarIngreso(@PathVariable Long id) {
        Recinto actualizado = service.registrarIngreso(id);
        return ResponseEntity.ok(ApiResponse.<RecintoDTO>builder()
                .success(true).message("Ingreso registrado").data(toDTO(actualizado)).build());
    }

    
    @PostMapping("/{id}/registrar-egreso")
    public ResponseEntity<ApiResponse<RecintoDTO>> registrarEgreso(@PathVariable Long id) {
        Recinto actualizado = service.registrarEgreso(id);
        return ResponseEntity.ok(ApiResponse.<RecintoDTO>builder()
                .success(true).message("Egreso registrado").data(toDTO(actualizado)).build());
    }

    // ── CONVERSIÓN ────────────────────────────────────────────────────────────

    private RecintoDTO toDTO(Recinto e) {
        RecintoDTO dto = new RecintoDTO();
        dto.setId(e.getId());
        dto.setNombre(e.getNombre());
        dto.setCapacidadMaxima(e.getCapacidadMaxima());
        dto.setCapacidad(e.getCapacidadMaxima());
        dto.setOcupacionActual(e.getOcupacionActual());
        if (e.getZonas() != null) {
            dto.setZonas(e.getZonas().stream().map(p -> {
                ZonaDTO pdto = new ZonaDTO();
                pdto.setId(p.getId());
                pdto.setNombre(p.getNombre());
                return pdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Recinto toEntity(RecintoDTO dto) {
        Recinto e = new Recinto();
        e.setNombre(dto.getNombre());
        // Soporta tanto 'capacidad' (alias) como 'capacidadMaxima'
        Integer cap = dto.getCapacidadMaxima() != null ? dto.getCapacidadMaxima() : dto.getCapacidad();
        e.setCapacidadMaxima(cap);
        e.setOcupacionActual(dto.getOcupacionActual() != null ? dto.getOcupacionActual() : 0);
        if (dto.getZonas() != null) {
            e.setZonas(dto.getZonas().stream().map(pdto -> {
                Zona p = new Zona();
                p.setNombre(pdto.getNombre());
                p.setRecinto(e);
                return p;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
