package com.carmeet.ms_venue_capacity.controller;

import com.carmeet.ms_venue_capacity.dto.ApiResponse;
import com.carmeet.ms_venue_capacity.model.Recinto;
import com.carmeet.ms_venue_capacity.model.Zona;
import com.carmeet.ms_venue_capacity.dto.RecintoDTO;
import com.carmeet.ms_venue_capacity.dto.ZonaDTO;
import com.carmeet.ms_venue_capacity.service.RecintoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Recintos", description = "Gestión de recintos, zonas y control de aforo")
@RestController
@RequestMapping("/api/v1/recintos")
@RequiredArgsConstructor
public class RecintoController {

    private final RecintoService service;

    @Operation(summary = "Listar todos los recintos", description = "Retorna la lista completa de recintos registrados")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente") })
    @GetMapping
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<RecintoDTO>>>> listar() {
        List<EntityModel<RecintoDTO>> lista = service.listar().stream().map(this::toDTO).map(dto -> {
            return EntityModel.of(dto,
                    linkTo(methodOn(RecintoController.class).obtenerPorId(dto.getId())).withSelfRel(),
                    linkTo(methodOn(RecintoController.class).listar()).withRel("all"));
        }).collect(Collectors.toList());

        CollectionModel<EntityModel<RecintoDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(RecintoController.class).listar()).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<RecintoDTO>>>builder().success(true).message("Listado").data(recurso).build());
    }

    @Operation(summary = "Obtener recinto por ID", description = "Retorna un recinto especifico por su identificador")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recinto encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recinto no encontrado") })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EntityModel<RecintoDTO>>> obtenerPorId(
            @Parameter(description = "ID del recinto", example = "1") @PathVariable Long id) {
        RecintoDTO dto = toDTO(service.obtenerPorId(id));
        EntityModel<RecintoDTO> recurso = EntityModel.of(dto);
        recurso.add(linkTo(methodOn(RecintoController.class).obtenerPorId(id)).withSelfRel());
        recurso.add(linkTo(methodOn(RecintoController.class).listar()).withRel("all"));

        return ResponseEntity.ok(ApiResponse.<EntityModel<RecintoDTO>>builder().success(true).message("Encontrado").data(recurso).build());
    }

    @Operation(summary = "Crear recinto", description = "Crea un nuevo recinto con sus zonas opcionales")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recinto creado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos") })
    @PostMapping
    public ResponseEntity<ApiResponse<EntityModel<RecintoDTO>>> guardar(@Valid @RequestBody RecintoDTO req) {
        Recinto nuevo = service.guardar(toEntity(req));
        RecintoDTO dto = toDTO(nuevo);
        EntityModel<RecintoDTO> recurso = EntityModel.of(dto);
        recurso.add(linkTo(methodOn(RecintoController.class).obtenerPorId(dto.getId())).withSelfRel());
        recurso.add(linkTo(methodOn(RecintoController.class).listar()).withRel("all"));

        return ResponseEntity.status(201).body(ApiResponse.<EntityModel<RecintoDTO>>builder().success(true).message("Creado").data(recurso).build());
    }

    @Operation(summary = "Actualizar recinto", description = "Actualiza los datos de un recinto existente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recinto actualizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recinto no encontrado") })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EntityModel<RecintoDTO>>> actualizar(
            @Parameter(description = "ID del recinto a actualizar", example = "1") @PathVariable Long id,
            @Valid @RequestBody RecintoDTO req) {
        Recinto actualizado = service.actualizar(id, toEntity(req));
        RecintoDTO dto = toDTO(actualizado);
        EntityModel<RecintoDTO> recurso = EntityModel.of(dto);
        recurso.add(linkTo(methodOn(RecintoController.class).obtenerPorId(dto.getId())).withSelfRel());
        recurso.add(linkTo(methodOn(RecintoController.class).listar()).withRel("all"));

        return ResponseEntity.ok(ApiResponse.<EntityModel<RecintoDTO>>builder().success(true).message("Actualizado").data(recurso).build());
    }

    @Operation(summary = "Eliminar recinto", description = "Elimina un recinto por su ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recinto eliminado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recinto no encontrado") })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @Parameter(description = "ID del recinto a eliminar", example = "1") @PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Eliminado").build());
    }

    @Operation(summary = "Listar zonas de un recinto", description = "Retorna las zonas asociadas a un recinto especifico")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Zonas obtenidas"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recinto no encontrado") })
    @GetMapping("/{id}/zonas")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<ZonaDTO>>>> listarZonas(
            @Parameter(description = "ID del recinto", example = "1") @PathVariable Long id) {
        List<EntityModel<ZonaDTO>> zonas = service.listarZonas(id).stream()
                .map(z -> {
                    ZonaDTO dto = new ZonaDTO();
                    dto.setId(z.getId());
                    dto.setNombre(z.getNombre());
                    return EntityModel.of(dto, linkTo(methodOn(RecintoController.class).listarZonas(id)).withRel("zonas"));
                }).collect(Collectors.toList());

        CollectionModel<EntityModel<ZonaDTO>> recurso = CollectionModel.of(zonas,
                linkTo(methodOn(RecintoController.class).listarZonas(id)).withSelfRel(),
                linkTo(methodOn(RecintoController.class).obtenerPorId(id)).withRel("recinto"));

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<ZonaDTO>>>builder()
                .success(true).message("Zonas del recinto " + id).data(recurso).build());
    }

    @Operation(summary = "Consultar disponibilidad de recinto", description = "Retorna capacidad maxima, ocupacion actual y plazas libres")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Disponibilidad obtenida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recinto no encontrado") })
    @GetMapping("/{id}/disponibilidad")
    public ResponseEntity<ApiResponse<EntityModel<Map<String, Object>>>> consultarDisponibilidad(
            @Parameter(description = "ID del recinto", example = "1") @PathVariable Long id) {
        Map<String, Object> info = service.consultarDisponibilidad(id);
        EntityModel<Map<String, Object>> recurso = EntityModel.of(info);
        recurso.add(linkTo(methodOn(RecintoController.class).consultarDisponibilidad(id)).withSelfRel());
        recurso.add(linkTo(methodOn(RecintoController.class).obtenerPorId(id)).withRel("recinto"));

        return ResponseEntity.ok(ApiResponse.<EntityModel<Map<String, Object>>>builder()
                .success(true).message("Disponibilidad del recinto").data(recurso).build());
    }

    @Operation(summary = "Registrar ingreso al recinto", description = "Incrementa la ocupacion actual del recinto en 1. Retorna 400 si el recinto esta lleno")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ingreso registrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Recinto lleno"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recinto no encontrado") })
    @PostMapping("/{id}/registrar-ingreso")
    public ResponseEntity<ApiResponse<EntityModel<RecintoDTO>>> registrarIngreso(
            @Parameter(description = "ID del recinto", example = "1") @PathVariable Long id) {
        Recinto actualizado = service.registrarIngreso(id);
        RecintoDTO dto = toDTO(actualizado);
        EntityModel<RecintoDTO> recurso = EntityModel.of(dto);
        recurso.add(linkTo(methodOn(RecintoController.class).consultarDisponibilidad(id)).withRel("disponibilidad"));
        recurso.add(linkTo(methodOn(RecintoController.class).obtenerPorId(id)).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<EntityModel<RecintoDTO>>builder()
                .success(true).message("Ingreso registrado").data(recurso).build());
    }

    @Operation(summary = "Registrar egreso del recinto", description = "Decrementa la ocupacion actual del recinto en 1")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Egreso registrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recinto no encontrado") })
    @PostMapping("/{id}/registrar-egreso")
    public ResponseEntity<ApiResponse<EntityModel<RecintoDTO>>> registrarEgreso(
            @Parameter(description = "ID del recinto", example = "1") @PathVariable Long id) {
        Recinto actualizado = service.registrarEgreso(id);
        RecintoDTO dto = toDTO(actualizado);
        EntityModel<RecintoDTO> recurso = EntityModel.of(dto);
        recurso.add(linkTo(methodOn(RecintoController.class).consultarDisponibilidad(id)).withRel("disponibilidad"));
        recurso.add(linkTo(methodOn(RecintoController.class).obtenerPorId(id)).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<EntityModel<RecintoDTO>>builder()
                .success(true).message("Egreso registrado").data(recurso).build());
    }

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
