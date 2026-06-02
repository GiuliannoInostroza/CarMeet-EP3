package com.carmeet.ms_analytics_report.controller;

import com.carmeet.ms_analytics_report.dto.ApiResponse;
import com.carmeet.ms_analytics_report.model.Reporte;
import com.carmeet.ms_analytics_report.model.Metrica;
import com.carmeet.ms_analytics_report.dto.ReporteDTO;
import com.carmeet.ms_analytics_report.dto.MetricaDTO;
import com.carmeet.ms_analytics_report.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService service;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReporteDTO>>> listar() {
        List<ReporteDTO> lista = service.listar().stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<ReporteDTO>>builder()
                .success(true).message("Listado").data(lista).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReporteDTO>> obtenerPorId(@PathVariable Long id) {
        ReporteDTO dto = toDTO(service.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.<ReporteDTO>builder()
                .success(true).message("Encontrado").data(dto).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReporteDTO>> guardar(@Valid @RequestBody ReporteDTO dto) {
        Reporte nuevo = service.guardar(toEntity(dto));
        return ResponseEntity.status(201).body(ApiResponse.<ReporteDTO>builder()
                .success(true).message("Creado").data(toDTO(nuevo)).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReporteDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReporteDTO dto) {
        Reporte actualizado = service.actualizar(id, toEntity(dto));
        return ResponseEntity.ok(ApiResponse.<ReporteDTO>builder()
                .success(true).message("Actualizado").data(toDTO(actualizado)).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Eliminado").build());
    }

    // ── MÉTODOS DE NEGOCIO ────────────────────────────────────────────────────

    
    @PostMapping("/generar/{eventoId}")
    public ResponseEntity<ApiResponse<ReporteDTO>> generarReporte(
            @PathVariable Long eventoId,
            HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        Reporte reporte = service.generarReporte(eventoId, bearer);
        return ResponseEntity.status(201).body(ApiResponse.<ReporteDTO>builder()
                .success(true)
                .message("Reporte generado para el evento " + eventoId)
                .data(toDTO(reporte)).build());
    }

    
    @GetMapping("/metricas/resumen")
    public ResponseEntity<ApiResponse<List<MetricaDTO>>> resumenMetricas() {
        List<MetricaDTO> metricas = service.resumenMetricas();
        return ResponseEntity.ok(ApiResponse.<List<MetricaDTO>>builder()
                .success(true).message("Resumen de métricas").data(metricas).build());
    }

    // ── CONVERSIÓN ────────────────────────────────────────────────────────────

    private ReporteDTO toDTO(Reporte e) {
        ReporteDTO dto = new ReporteDTO();
        dto.setId(e.getId());
        dto.setEventoId(e.getEventoId());
        dto.setTotalEventos(e.getTotalEventos());
        dto.setTotalTickets(e.getTotalTickets());
        dto.setTotalInscripciones(e.getTotalInscripciones());
        dto.setFechaGeneracion(e.getFechaGeneracion());
        if (e.getMetricas() != null) {
            dto.setMetricas(e.getMetricas().stream().map(p -> {
                MetricaDTO pdto = new MetricaDTO();
                pdto.setId(p.getId());
                pdto.setNombre(p.getNombre());
                pdto.setValor(p.getValor());
                return pdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Reporte toEntity(ReporteDTO dto) {
        Reporte e = new Reporte();
        e.setEventoId(dto.getEventoId());
        e.setTotalEventos(dto.getTotalEventos());
        e.setTotalTickets(dto.getTotalTickets());
        e.setTotalInscripciones(dto.getTotalInscripciones());
        e.setFechaGeneracion(dto.getFechaGeneracion());
        if (dto.getMetricas() != null) {
            e.setMetricas(dto.getMetricas().stream().map(pdto -> {
                Metrica p = new Metrica();
                p.setNombre(pdto.getNombre());
                p.setValor(pdto.getValor());
                p.setReporte(e);
                return p;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
