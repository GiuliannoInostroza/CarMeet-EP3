package com.carmeet.ms_vehicle_registry.controller;

import com.carmeet.ms_vehicle_registry.dto.ApiResponse;
import com.carmeet.ms_vehicle_registry.model.Vehiculo;
import com.carmeet.ms_vehicle_registry.model.Mantenimiento;
import com.carmeet.ms_vehicle_registry.dto.VehiculoDTO;
import com.carmeet.ms_vehicle_registry.dto.MantenimientoDTO;
import com.carmeet.ms_vehicle_registry.service.VehiculoService;
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
import java.util.stream.Collectors;

@Tag(name = "Vehículos", description = "Registro de vehículos y su historial de mantenimientos")
@RestController
@RequestMapping("/api/v1/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService service;

    @Operation(summary = "Listar todos los vehículos", description = "Retorna la lista completa de vehículos registrados")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente") })
    @GetMapping
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<VehiculoDTO>>>> listar() {
        List<EntityModel<VehiculoDTO>> lista = service.listar().stream().map(this::toDTO).map(dto -> {
            return EntityModel.of(dto,
                    linkTo(methodOn(VehiculoController.class).obtenerPorId(dto.getId())).withSelfRel(),
                    linkTo(methodOn(VehiculoController.class).listar()).withRel("all"));
        }).collect(Collectors.toList());

        CollectionModel<EntityModel<VehiculoDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(VehiculoController.class).listar()).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<VehiculoDTO>>>builder().success(true).message("Listado").data(recurso).build());
    }

    @Operation(summary = "Obtener vehículo por ID", description = "Retorna un vehículo especifico por su identificador")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehículo encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehículo no encontrado") })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EntityModel<VehiculoDTO>>> obtenerPorId(
            @Parameter(description = "ID del vehículo", example = "1") @PathVariable Long id) {
        VehiculoDTO dto = toDTO(service.obtenerPorId(id));
        EntityModel<VehiculoDTO> recurso = EntityModel.of(dto);
        recurso.add(linkTo(methodOn(VehiculoController.class).obtenerPorId(id)).withSelfRel());
        recurso.add(linkTo(methodOn(VehiculoController.class).listar()).withRel("all"));

        return ResponseEntity.ok(ApiResponse.<EntityModel<VehiculoDTO>>builder().success(true).message("Encontrado").data(recurso).build());
    }

    @Operation(summary = "Registrar vehículo", description = "Registra un nuevo vehículo en el sistema")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Vehículo creado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos") })
    @PostMapping
    public ResponseEntity<ApiResponse<EntityModel<VehiculoDTO>>> guardar(@Valid @RequestBody VehiculoDTO req) {
        Vehiculo nuevo = service.guardar(toEntity(req));
        VehiculoDTO dto = toDTO(nuevo);
        EntityModel<VehiculoDTO> recurso = EntityModel.of(dto);
        recurso.add(linkTo(methodOn(VehiculoController.class).obtenerPorId(dto.getId())).withSelfRel());
        recurso.add(linkTo(methodOn(VehiculoController.class).listar()).withRel("all"));

        return ResponseEntity.status(201).body(ApiResponse.<EntityModel<VehiculoDTO>>builder().success(true).message("Creado").data(recurso).build());
    }

    @Operation(summary = "Actualizar vehículo", description = "Actualiza los datos de un vehículo existente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehículo actualizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehículo no encontrado") })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EntityModel<VehiculoDTO>>> actualizar(
            @Parameter(description = "ID del vehículo a actualizar", example = "1") @PathVariable Long id,
            @Valid @RequestBody VehiculoDTO req) {
        Vehiculo actualizado = service.actualizar(id, toEntity(req));
        VehiculoDTO dto = toDTO(actualizado);
        EntityModel<VehiculoDTO> recurso = EntityModel.of(dto);
        recurso.add(linkTo(methodOn(VehiculoController.class).obtenerPorId(dto.getId())).withSelfRel());
        recurso.add(linkTo(methodOn(VehiculoController.class).listar()).withRel("all"));

        return ResponseEntity.ok(ApiResponse.<EntityModel<VehiculoDTO>>builder().success(true).message("Actualizado").data(recurso).build());
    }

    @Operation(summary = "Eliminar vehículo", description = "Elimina un vehículo por su ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehículo eliminado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehículo no encontrado") })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @Parameter(description = "ID del vehículo a eliminar", example = "1") @PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Eliminado").build());
    }

    @Operation(summary = "Listar mantenimientos de un vehículo", description = "Retorna el historial completo de mantenimientos de un vehículo")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Historial obtenido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehículo no encontrado") })
    @GetMapping("/{id}/mantenimientos")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<MantenimientoDTO>>>> listarMantenimientos(
            @Parameter(description = "ID del vehículo", example = "1") @PathVariable Long id) {
        List<EntityModel<MantenimientoDTO>> lista = service.listarMantenimientos(id).stream()
                .map(m -> {
                    MantenimientoDTO dto = new MantenimientoDTO();
                    dto.setId(m.getId());
                    dto.setDescripcion(m.getDescripcion());
                    return EntityModel.of(dto, linkTo(methodOn(VehiculoController.class).listarMantenimientos(id)).withRel("mantenimientos"));
                }).collect(Collectors.toList());

        CollectionModel<EntityModel<MantenimientoDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(VehiculoController.class).listarMantenimientos(id)).withSelfRel(),
                linkTo(methodOn(VehiculoController.class).obtenerPorId(id)).withRel("vehiculo"));

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<MantenimientoDTO>>>builder()
                .success(true).message("Historial de mantenimientos").data(recurso).build());
    }

    @Operation(summary = "Agregar mantenimiento", description = "Registra un nuevo mantenimiento al vehículo sin reemplazar los existentes")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Mantenimiento agregado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehículo no encontrado") })
    @PostMapping("/{id}/mantenimientos")
    public ResponseEntity<ApiResponse<EntityModel<VehiculoDTO>>> agregarMantenimiento(
            @Parameter(description = "ID del vehículo", example = "1") @PathVariable Long id,
            @Valid @RequestBody MantenimientoDTO req) {
        Mantenimiento m = new Mantenimiento();
        m.setDescripcion(req.getDescripcion());
        Vehiculo actualizado = service.agregarMantenimiento(id, m);
        
        VehiculoDTO dto = toDTO(actualizado);
        EntityModel<VehiculoDTO> recurso = EntityModel.of(dto);
        recurso.add(linkTo(methodOn(VehiculoController.class).obtenerPorId(dto.getId())).withSelfRel());
        recurso.add(linkTo(methodOn(VehiculoController.class).listarMantenimientos(dto.getId())).withRel("mantenimientos"));

        return ResponseEntity.status(201).body(ApiResponse.<EntityModel<VehiculoDTO>>builder()
                .success(true).message("Mantenimiento agregado").data(recurso).build());
    }

    @Operation(summary = "Buscar vehículos por modelo", description = "Busca vehículos cuyo modelo contenga el texto indicado")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Resultados obtenidos") })
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<VehiculoDTO>>>> buscarPorModelo(
            @Parameter(description = "Modelo del vehículo a buscar", example = "Civic") @RequestParam String modelo) {
        List<EntityModel<VehiculoDTO>> lista = service.buscarPorModelo(modelo).stream()
                .map(this::toDTO).map(dto -> {
                    return EntityModel.of(dto,
                            linkTo(methodOn(VehiculoController.class).obtenerPorId(dto.getId())).withSelfRel(),
                            linkTo(methodOn(VehiculoController.class).listar()).withRel("all"));
                }).collect(Collectors.toList());

        CollectionModel<EntityModel<VehiculoDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(VehiculoController.class).buscarPorModelo(modelo)).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<VehiculoDTO>>>builder()
                .success(true).message("Resultados para: " + modelo).data(recurso).build());
    }

    private VehiculoDTO toDTO(Vehiculo e) {
        VehiculoDTO dto = new VehiculoDTO();
        dto.setId(e.getId());
        dto.setMarca(e.getMarca());
        dto.setModelo(e.getModelo());
        dto.setAnio(e.getAnio());
        if (e.getMantenimientos() != null) {
            dto.setMantenimientos(e.getMantenimientos().stream().map(p -> {
                MantenimientoDTO pdto = new MantenimientoDTO();
                pdto.setId(p.getId());
                pdto.setDescripcion(p.getDescripcion());
                return pdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Vehiculo toEntity(VehiculoDTO dto) {
        Vehiculo e = new Vehiculo();
        e.setMarca(dto.getMarca());
        e.setModelo(dto.getModelo());
        e.setAnio(dto.getAnio());
        if (dto.getMantenimientos() != null) {
            e.setMantenimientos(dto.getMantenimientos().stream().map(pdto -> {
                Mantenimiento p = new Mantenimiento();
                p.setDescripcion(pdto.getDescripcion());
                p.setVehiculo(e);
                return p;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
