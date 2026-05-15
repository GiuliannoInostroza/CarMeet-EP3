package com.carmeet.ms_live_scoreboard.controller;

import com.carmeet.ms_live_scoreboard.dto.ApiResponse;
import com.carmeet.ms_live_scoreboard.model.Puntuacion;
import com.carmeet.ms_live_scoreboard.model.DetallePuntuacion;
import com.carmeet.ms_live_scoreboard.dto.PuntuacionDTO;
import com.carmeet.ms_live_scoreboard.dto.DetallePuntuacionDTO;
import com.carmeet.ms_live_scoreboard.service.PuntuacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/puntuaciones")
@RequiredArgsConstructor
public class PuntuacionController {

    private final PuntuacionService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PuntuacionDTO>>> listar() {
        List<PuntuacionDTO> lista = service.listar().stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<PuntuacionDTO>>builder().success(true).message("Listado").data(lista).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PuntuacionDTO>> obtenerPorId(@PathVariable Long id) {
        PuntuacionDTO dto = toDTO(service.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.<PuntuacionDTO>builder().success(true).message("Encontrado").data(dto).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PuntuacionDTO>> guardar(@Valid @RequestBody PuntuacionDTO dto) {
        Puntuacion nuevo = service.guardar(toEntity(dto));
        return ResponseEntity.status(201).body(ApiResponse.<PuntuacionDTO>builder().success(true).message("Creado").data(toDTO(nuevo)).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PuntuacionDTO>> actualizar(@PathVariable Long id, @Valid @RequestBody PuntuacionDTO dto) {
        Puntuacion actualizado = service.actualizar(id, toEntity(dto));
        return ResponseEntity.ok(ApiResponse.<PuntuacionDTO>builder().success(true).message("Actualizado").data(toDTO(actualizado)).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Eliminado").build());
    }

    private PuntuacionDTO toDTO(Puntuacion e) {
        PuntuacionDTO dto = new PuntuacionDTO();
        dto.setId(e.getId());
        dto.setInscripcionId(e.getInscripcionId());
        dto.setPuntos(e.getPuntos());
        if(e.getDetalles() != null) {
            dto.setDetalles(e.getDetalles().stream().map(p -> {
                DetallePuntuacionDTO pdto = new DetallePuntuacionDTO();
                pdto.setId(p.getId());
                pdto.setCategoria(p.getCategoria());
                pdto.setPuntosAsignados(p.getPuntosAsignados());
                pdto.setDescripcion(p.getDescripcion());
                return pdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Puntuacion toEntity(PuntuacionDTO dto) {
        Puntuacion e = new Puntuacion();
        e.setInscripcionId(dto.getInscripcionId());
        e.setPuntos(dto.getPuntos());
        if(dto.getDetalles() != null) {
            e.setDetalles(dto.getDetalles().stream().map(pdto -> {
                DetallePuntuacion p = new DetallePuntuacion();
                p.setCategoria(pdto.getCategoria());
                p.setPuntosAsignados(pdto.getPuntosAsignados());
                p.setDescripcion(pdto.getDescripcion());
                p.setPuntuacion(e);
                return p;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
