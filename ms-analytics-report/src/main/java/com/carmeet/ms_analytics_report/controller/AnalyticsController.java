package com.carmeet.ms_analytics_report.controller;

import com.carmeet.ms_analytics_report.dto.ApiResponse;
import com.carmeet.ms_analytics_report.model.Reporte;
import com.carmeet.ms_analytics_report.model.Metrica;
import com.carmeet.ms_analytics_report.dto.ReporteDTO;
import com.carmeet.ms_analytics_report.dto.MetricaDTO;
import com.carmeet.ms_analytics_report.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Reportes y Analíticas", description = "Generación de reportes de eventos y resumen de métricas del sistema")
@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService service;

    @Operation(summary = "Listar todos los reportes", description = "Retorna la lista completa de reportes generados")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente") })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<ReporteDTO>>>> listar() {
        List<EntityModel<ReporteDTO>> lista = service.listar().stream().map(this::toDTO).map(dto -> {
            return EntityModel.of(dto,
                    linkTo(methodOn(AnalyticsController.class).obtenerPorId(dto.getId())).withSelfRel(),
                    linkTo(methodOn(AnalyticsController.class).listar()).withRel("all"));
        }).collect(Collectors.toList());

        CollectionModel<EntityModel<ReporteDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(AnalyticsController.class).listar()).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<ReporteDTO>>>builder()
                .success(true).message("Listado").data(recurso).build());
    }

    @Operation(summary = "Obtener reporte por ID", description = "Retorna un reporte especifico por su identificador")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Reporte no encontrado") })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<EntityModel<ReporteDTO>>> obtenerPorId(
            @Parameter(description = "ID del reporte", example = "1") @PathVariable Long id) {
        ReporteDTO dto = toDTO(service.obtenerPorId(id));
        EntityModel<ReporteDTO> recurso = EntityModel.of(dto);
        recurso.add(linkTo(methodOn(AnalyticsController.class).obtenerPorId(id)).withSelfRel());
        recurso.add(linkTo(methodOn(AnalyticsController.class).listar()).withRel("all"));

        return ResponseEntity.ok(ApiResponse.<EntityModel<ReporteDTO>>builder()
                .success(true).message("Encontrado").data(recurso).build());
    }

    @Operation(summary = "Crear reporte manualmente", description = "Guarda un registro de reporte directamente en base de datos")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Reporte creado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos") })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<ReporteDTO>>> guardar(@Valid @RequestBody ReporteDTO req) {
        Reporte nuevo = service.guardar(toEntity(req));
        ReporteDTO dto = toDTO(nuevo);
        EntityModel<ReporteDTO> recurso = EntityModel.of(dto);
        recurso.add(linkTo(methodOn(AnalyticsController.class).obtenerPorId(dto.getId())).withSelfRel());
        recurso.add(linkTo(methodOn(AnalyticsController.class).listar()).withRel("all"));

        return ResponseEntity.status(201).body(ApiResponse.<EntityModel<ReporteDTO>>builder()
                .success(true).message("Creado").data(recurso).build());
    }

    @Operation(summary = "Actualizar reporte", description = "Actualiza los datos de un reporte existente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte actualizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Reporte no encontrado") })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<ReporteDTO>>> actualizar(
            @Parameter(description = "ID del reporte a actualizar", example = "1") @PathVariable Long id,
            @Valid @RequestBody ReporteDTO req) {
        Reporte actualizado = service.actualizar(id, toEntity(req));
        ReporteDTO dto = toDTO(actualizado);
        EntityModel<ReporteDTO> recurso = EntityModel.of(dto);
        recurso.add(linkTo(methodOn(AnalyticsController.class).obtenerPorId(dto.getId())).withSelfRel());
        recurso.add(linkTo(methodOn(AnalyticsController.class).listar()).withRel("all"));

        return ResponseEntity.ok(ApiResponse.<EntityModel<ReporteDTO>>builder()
                .success(true).message("Actualizado").data(recurso).build());
    }

    @Operation(summary = "Eliminar reporte", description = "Elimina un reporte por su ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte eliminado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Reporte no encontrado") })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @Parameter(description = "ID del reporte a eliminar", example = "1") @PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Eliminado").build());
    }

    @Operation(summary = "Generar reporte de evento", description = "Recopila información de tickets e inscripciones asociados a un evento vía WebClient y crea el reporte")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Reporte generado exitosamente") })
    @PostMapping("/generar/{eventoId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<ReporteDTO>>> generarReporte(
            @Parameter(description = "ID del evento a reportar", example = "1") @PathVariable Long eventoId,
            HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        Reporte reporte = service.generarReporte(eventoId, bearer);
        ReporteDTO dto = toDTO(reporte);
        EntityModel<ReporteDTO> recurso = EntityModel.of(dto);
        recurso.add(linkTo(methodOn(AnalyticsController.class).obtenerPorId(dto.getId())).withSelfRel());

        return ResponseEntity.status(201).body(ApiResponse.<EntityModel<ReporteDTO>>builder()
                .success(true)
                .message("Reporte generado para el evento " + eventoId)
                .data(recurso).build());
    }

    @Operation(summary = "Resumen de métricas", description = "Calcula métricas agrupadas a partir de todos los reportes almacenados")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Resumen de métricas obtenido") })
    @GetMapping("/metricas/resumen")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<MetricaDTO>>>> resumenMetricas() {
        List<EntityModel<MetricaDTO>> metricas = service.resumenMetricas().stream()
                .map(dto -> EntityModel.of(dto, linkTo(methodOn(AnalyticsController.class).resumenMetricas()).withRel("resumen")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<MetricaDTO>> recurso = CollectionModel.of(metricas,
                linkTo(methodOn(AnalyticsController.class).resumenMetricas()).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<MetricaDTO>>>builder()
                .success(true).message("Resumen de métricas").data(recurso).build());
    }

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
