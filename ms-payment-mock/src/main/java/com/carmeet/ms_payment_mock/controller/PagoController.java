package com.carmeet.ms_payment_mock.controller;

import com.carmeet.ms_payment_mock.dto.ApiResponse;
import com.carmeet.ms_payment_mock.model.Pago;
import com.carmeet.ms_payment_mock.model.TransaccionLog;
import com.carmeet.ms_payment_mock.dto.PagoDTO;
import com.carmeet.ms_payment_mock.dto.TransaccionLogDTO;
import com.carmeet.ms_payment_mock.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PagoDTO>>> listar() {
        List<PagoDTO> lista = service.listar().stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<PagoDTO>>builder().success(true).message("Listado").data(lista).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PagoDTO>> obtenerPorId(@PathVariable Long id) {
        PagoDTO dto = toDTO(service.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.<PagoDTO>builder().success(true).message("Encontrado").data(dto).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PagoDTO>> guardar(@Valid @RequestBody PagoDTO dto) {
        Pago nuevo = service.guardar(toEntity(dto));
        return ResponseEntity.status(201).body(ApiResponse.<PagoDTO>builder().success(true).message("Creado").data(toDTO(nuevo)).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PagoDTO>> actualizar(@PathVariable Long id, @Valid @RequestBody PagoDTO dto) {
        Pago actualizado = service.actualizar(id, toEntity(dto));
        return ResponseEntity.ok(ApiResponse.<PagoDTO>builder().success(true).message("Actualizado").data(toDTO(actualizado)).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Eliminado").build());
    }

    private PagoDTO toDTO(Pago e) {
        PagoDTO dto = new PagoDTO();
        dto.setId(e.getId());
        dto.setTicketId(e.getTicketId());
        dto.setMonto(e.getMonto());
        if(e.getLogs() != null) {
            dto.setLogs(e.getLogs().stream().map(p -> {
                TransaccionLogDTO pdto = new TransaccionLogDTO();
                pdto.setId(p.getId());
                pdto.setEstado(p.getEstado());
                return pdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Pago toEntity(PagoDTO dto) {
        Pago e = new Pago();
        e.setTicketId(dto.getTicketId());
        e.setMonto(dto.getMonto());
        if(dto.getLogs() != null) {
            e.setLogs(dto.getLogs().stream().map(pdto -> {
                TransaccionLog p = new TransaccionLog();
                p.setEstado(pdto.getEstado());
                p.setPago(e);
                return p;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
