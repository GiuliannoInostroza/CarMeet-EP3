package com.carmeet.ms_ticketing.controller;

import com.carmeet.ms_ticketing.dto.ApiResponse;
import com.carmeet.ms_ticketing.model.Ticket;
import com.carmeet.ms_ticketing.model.Beneficio;
import com.carmeet.ms_ticketing.dto.TicketDTO;
import com.carmeet.ms_ticketing.dto.BeneficioDTO;
import com.carmeet.ms_ticketing.service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService service;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketDTO>>> listar() {
        List<TicketDTO> lista = service.listar().stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<TicketDTO>>builder()
                .success(true).message("Listado").data(lista).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDTO>> obtenerPorId(@PathVariable Long id) {
        TicketDTO dto = toDTO(service.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.<TicketDTO>builder()
                .success(true).message("Encontrado").data(dto).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketDTO>> guardar(
            @Valid @RequestBody TicketDTO dto,
            HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        Ticket nuevo = service.guardar(toEntity(dto), bearer);
        return ResponseEntity.status(201).body(ApiResponse.<TicketDTO>builder()
                .success(true).message("Ticket creado").data(toDTO(nuevo)).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TicketDTO dto) {
        Ticket actualizado = service.actualizar(id, toEntity(dto));
        return ResponseEntity.ok(ApiResponse.<TicketDTO>builder()
                .success(true).message("Actualizado").data(toDTO(actualizado)).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Eliminado").build());
    }

    // ── MÉTODOS DE NEGOCIO ────────────────────────────────────────────────────

    
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<ApiResponse<List<TicketDTO>>> porEvento(@PathVariable Long eventoId) {
        List<TicketDTO> lista = service.obtenerPorEventoId(eventoId).stream()
                .map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<TicketDTO>>builder()
                .success(true).message("Tickets del evento " + eventoId).data(lista).build());
    }

    
    @GetMapping("/usuario/{username}")
    public ResponseEntity<ApiResponse<List<TicketDTO>>> porUsuario(@PathVariable String username) {
        List<TicketDTO> lista = service.obtenerPorUsername(username).stream()
                .map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<TicketDTO>>builder()
                .success(true).message("Tickets del usuario: " + username).data(lista).build());
    }

    
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<TicketDTO>> cancelar(@PathVariable Long id) {
        TicketDTO dto = toDTO(service.cancelar(id));
        return ResponseEntity.ok(ApiResponse.<TicketDTO>builder()
                .success(true).message("Ticket cancelado").data(dto).build());
    }

    
    @PatchMapping("/{id}/pagar")
    public ResponseEntity<ApiResponse<TicketDTO>> pagar(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        String metodoPago = (body != null) ? body.get("metodoPago") : "TARJETA";
        TicketDTO dto = toDTO(service.pagar(id, metodoPago, bearer));
        return ResponseEntity.ok(ApiResponse.<TicketDTO>builder()
                .success(true).message("Pago procesado exitosamente").data(dto).build());
    }

    // ── CONVERSIÓN ────────────────────────────────────────────────────────────

    private TicketDTO toDTO(Ticket e) {
        TicketDTO dto = new TicketDTO();
        dto.setId(e.getId());
        dto.setEventoId(e.getEventoId());
        dto.setPrecio(e.getPrecio());
        dto.setCategoria(e.getCategoria());
        dto.setEstado(e.getEstado());
        dto.setUsername(e.getUsername());
        if (e.getBeneficios() != null) {
            dto.setBeneficios(e.getBeneficios().stream().map(p -> {
                BeneficioDTO pdto = new BeneficioDTO();
                pdto.setId(p.getId());
                pdto.setNombre(p.getNombre());
                pdto.setDescripcion(p.getDescripcion());
                return pdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Ticket toEntity(TicketDTO dto) {
        Ticket e = new Ticket();
        e.setEventoId(dto.getEventoId());
        e.setPrecio(dto.getPrecio());
        e.setCategoria(dto.getCategoria());
        e.setEstado(dto.getEstado() != null ? dto.getEstado() : "PENDIENTE");
        e.setUsername(dto.getUsername());
        if (dto.getBeneficios() != null) {
            e.setBeneficios(dto.getBeneficios().stream().map(pdto -> {
                Beneficio p = new Beneficio();
                p.setNombre(pdto.getNombre());
                p.setDescripcion(pdto.getDescripcion());
                p.setTicket(e);
                return p;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
