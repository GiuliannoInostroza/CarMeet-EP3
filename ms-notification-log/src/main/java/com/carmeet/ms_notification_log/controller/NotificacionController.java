package com.carmeet.ms_notification_log.controller;

import com.carmeet.ms_notification_log.dto.ApiResponse;
import com.carmeet.ms_notification_log.model.Notificacion;
import com.carmeet.ms_notification_log.model.Adjunto;
import com.carmeet.ms_notification_log.dto.NotificacionDTO;
import com.carmeet.ms_notification_log.dto.AdjuntoDTO;
import com.carmeet.ms_notification_log.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificacionDTO>>> listar() {
        List<NotificacionDTO> lista = service.listar().stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<NotificacionDTO>>builder().success(true).message("Listado").data(lista).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificacionDTO>> obtenerPorId(@PathVariable Long id) {
        NotificacionDTO dto = toDTO(service.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.<NotificacionDTO>builder().success(true).message("Encontrado").data(dto).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NotificacionDTO>> guardar(@Valid @RequestBody NotificacionDTO dto) {
        Notificacion nuevo = service.guardar(toEntity(dto));
        return ResponseEntity.status(201).body(ApiResponse.<NotificacionDTO>builder().success(true).message("Creado").data(toDTO(nuevo)).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificacionDTO>> actualizar(@PathVariable Long id, @Valid @RequestBody NotificacionDTO dto) {
        Notificacion actualizado = service.actualizar(id, toEntity(dto));
        return ResponseEntity.ok(ApiResponse.<NotificacionDTO>builder().success(true).message("Actualizado").data(toDTO(actualizado)).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Eliminado").build());
    }

    private NotificacionDTO toDTO(Notificacion e) {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setId(e.getId());
        dto.setDestinatario(e.getDestinatario());
        dto.setMensaje(e.getMensaje());
        if(e.getAdjuntos() != null) {
            dto.setAdjuntos(e.getAdjuntos().stream().map(p -> {
                AdjuntoDTO pdto = new AdjuntoDTO();
                pdto.setId(p.getId());
                pdto.setRutaArchivo(p.getRutaArchivo());
                return pdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Notificacion toEntity(NotificacionDTO dto) {
        Notificacion e = new Notificacion();
        e.setDestinatario(dto.getDestinatario());
        e.setMensaje(dto.getMensaje());
        if(dto.getAdjuntos() != null) {
            e.setAdjuntos(dto.getAdjuntos().stream().map(pdto -> {
                Adjunto p = new Adjunto();
                p.setRutaArchivo(pdto.getRutaArchivo());
                return p;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
