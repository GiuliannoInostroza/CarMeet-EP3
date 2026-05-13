package com.carmeet.ms_ticketing.controller;

import com.carmeet.ms_ticketing.dto.ApiResponse;
import com.carmeet.ms_ticketing.model.Ticket;
import com.carmeet.ms_ticketing.model.Beneficio;
import com.carmeet.ms_ticketing.dto.TicketDTO;
import com.carmeet.ms_ticketing.dto.BeneficioDTO;
import com.carmeet.ms_ticketing.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketDTO>>> listar() {
        List<TicketDTO> lista = service.listar().stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<TicketDTO>>builder().success(true).message("Listado").data(lista).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDTO>> obtenerPorId(@PathVariable Long id) {
        TicketDTO dto = toDTO(service.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.<TicketDTO>builder().success(true).message("Encontrado").data(dto).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketDTO>> guardar(@Valid @RequestBody TicketDTO dto) {
        Ticket nuevo = service.guardar(toEntity(dto));
        return ResponseEntity.status(201).body(ApiResponse.<TicketDTO>builder().success(true).message("Creado").data(toDTO(nuevo)).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDTO>> actualizar(@PathVariable Long id, @Valid @RequestBody TicketDTO dto) {
        Ticket actualizado = service.actualizar(id, toEntity(dto));
        return ResponseEntity.ok(ApiResponse.<TicketDTO>builder().success(true).message("Actualizado").data(toDTO(actualizado)).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Eliminado").build());
    }

    private TicketDTO toDTO(Ticket e) {
        TicketDTO dto = new TicketDTO();
        dto.setId(e.getId());
        dto.setEventoId(e.getEventoId());
        dto.setPrecio(e.getPrecio());
        dto.setEstado(e.getEstado());
        dto.setUsername(e.getUsername());
        if(e.getBeneficios() != null) {
            dto.setBeneficios(e.getBeneficios().stream().map(p -> {
                BeneficioDTO pdto = new BeneficioDTO();
                pdto.setId(p.getId());
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
        e.setEstado(dto.getEstado());
        e.setUsername(dto.getUsername());
        if(dto.getBeneficios() != null) {
            e.setBeneficios(dto.getBeneficios().stream().map(pdto -> {
                Beneficio p = new Beneficio();
                p.setDescripcion(pdto.getDescripcion());
                return p;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
