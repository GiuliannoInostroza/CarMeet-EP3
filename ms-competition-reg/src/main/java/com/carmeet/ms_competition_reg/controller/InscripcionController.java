package com.carmeet.ms_competition_reg.controller;

import com.carmeet.ms_competition_reg.dto.ApiResponse;
import com.carmeet.ms_competition_reg.model.Inscripcion;
import com.carmeet.ms_competition_reg.model.Requisito;
import com.carmeet.ms_competition_reg.dto.InscripcionDTO;
import com.carmeet.ms_competition_reg.dto.RequisitoDTO;
import com.carmeet.ms_competition_reg.service.InscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionService service;

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
    public ResponseEntity<ApiResponse<InscripcionDTO>> guardar(@Valid @RequestBody InscripcionDTO dto) {
        Inscripcion nuevo = service.guardar(toEntity(dto));
        return ResponseEntity.status(201).body(ApiResponse.<InscripcionDTO>builder().success(true).message("Creado").data(toDTO(nuevo)).build());
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

    private InscripcionDTO toDTO(Inscripcion e) {
        InscripcionDTO dto = new InscripcionDTO();
        dto.setId(e.getId());
        dto.setVehiculoId(e.getVehiculoId());
        dto.setCategoria(e.getCategoria());
        dto.setUsername(e.getUsername());
        if(e.getRequisitos() != null) {
            dto.setRequisitos(e.getRequisitos().stream().map(p -> {
                RequisitoDTO pdto = new RequisitoDTO();
                pdto.setId(p.getId());
                pdto.setDescripcion(p.getDescripcion());
                return pdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Inscripcion toEntity(InscripcionDTO dto) {
        Inscripcion e = new Inscripcion();
        e.setVehiculoId(dto.getVehiculoId());
        e.setCategoria(dto.getCategoria());
        e.setUsername(dto.getUsername());
        if(dto.getRequisitos() != null) {
            e.setRequisitos(dto.getRequisitos().stream().map(pdto -> {
                Requisito p = new Requisito();
                p.setDescripcion(pdto.getDescripcion());
                p.setInscripcion(e);
                return p;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
