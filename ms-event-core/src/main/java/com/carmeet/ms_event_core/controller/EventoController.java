package com.carmeet.ms_event_core.controller;

import com.carmeet.ms_event_core.dto.ApiResponse;
import com.carmeet.ms_event_core.model.Evento;
import com.carmeet.ms_event_core.model.Patrocinador;
import com.carmeet.ms_event_core.dto.EventoDTO;
import com.carmeet.ms_event_core.dto.PatrocinadorDTO;
import com.carmeet.ms_event_core.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService service;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventoDTO>>> listar() {
        List<EventoDTO> lista = service.listar().stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<EventoDTO>>builder().success(true).message("Listado").data(lista).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventoDTO>> obtenerPorId(@PathVariable Long id) {
        EventoDTO dto = toDTO(service.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.<EventoDTO>builder().success(true).message("Encontrado").data(dto).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventoDTO>> guardar(@Valid @RequestBody EventoDTO dto) {
        Evento nuevo = service.guardar(toEntity(dto));
        return ResponseEntity.status(201).body(ApiResponse.<EventoDTO>builder().success(true).message("Creado").data(toDTO(nuevo)).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventoDTO>> actualizar(@PathVariable Long id, @Valid @RequestBody EventoDTO dto) {
        Evento actualizado = service.actualizar(id, toEntity(dto));
        return ResponseEntity.ok(ApiResponse.<EventoDTO>builder().success(true).message("Actualizado").data(toDTO(actualizado)).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Eliminado").build());
    }

    // ── MÉTODOS DE NEGOCIO ────────────────────────────────────────────────────

    
    @GetMapping("/{id}/patrocinadores")
    public ResponseEntity<ApiResponse<List<PatrocinadorDTO>>> listarPatrocinadores(@PathVariable Long id) {
        Evento evento = service.obtenerPorId(id);
        List<PatrocinadorDTO> lista = evento.getPatrocinadores().stream()
                .map(p -> {
                    PatrocinadorDTO dto = new PatrocinadorDTO();
                    dto.setId(p.getId());
                    dto.setNombre(p.getNombre());
                    dto.setNivel(p.getNivel());
                    return dto;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<PatrocinadorDTO>>builder()
                .success(true).message("Patrocinadores del evento " + id).data(lista).build());
    }

    
    @GetMapping("/proximos")
    public ResponseEntity<ApiResponse<List<EventoDTO>>> listarProximos() {
        List<EventoDTO> lista = service.listarProximos().stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<EventoDTO>>builder()
                .success(true).message("Eventos próximos").data(lista).build());
    }

    
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<EventoDTO>>> buscarPorNombre(@RequestParam String nombre) {
        List<EventoDTO> lista = service.buscarPorNombre(nombre).stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<EventoDTO>>builder()
                .success(true).message("Resultados para: " + nombre).data(lista).build());
    }

    // ── CONVERSIÓN ────────────────────────────────────────────────────────────

    private EventoDTO toDTO(Evento e) {
        EventoDTO dto = new EventoDTO();
        dto.setId(e.getId());
        dto.setNombre(e.getNombre());
        dto.setFecha(e.getFecha());
        dto.setUbicacion(e.getUbicacion());
        if (e.getPatrocinadores() != null) {
            dto.setPatrocinadores(e.getPatrocinadores().stream().map(p -> {
                PatrocinadorDTO pdto = new PatrocinadorDTO();
                pdto.setId(p.getId());
                pdto.setNombre(p.getNombre());
                pdto.setNivel(p.getNivel());   // BUG FIX: nivel era omitido
                return pdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Evento toEntity(EventoDTO dto) {
        Evento e = new Evento();
        e.setNombre(dto.getNombre());
        e.setFecha(dto.getFecha());
        e.setUbicacion(dto.getUbicacion());
        if (dto.getPatrocinadores() != null) {
            e.setPatrocinadores(dto.getPatrocinadores().stream().map(pdto -> {
                Patrocinador p = new Patrocinador();
                p.setNombre(pdto.getNombre());
                p.setNivel(pdto.getNivel());   // BUG FIX: nivel era omitido
                p.setEvento(e);
                return p;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
