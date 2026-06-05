package com.carmeet.ms_competition_reg.controller;

import com.carmeet.ms_competition_reg.dto.ApiResponse;
import com.carmeet.ms_competition_reg.model.Inscripcion;
import com.carmeet.ms_competition_reg.model.Requisito;
import com.carmeet.ms_competition_reg.dto.InscripcionDTO;
import com.carmeet.ms_competition_reg.dto.RequisitoDTO;
import com.carmeet.ms_competition_reg.service.InscripcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Inscripciones", description = "Registro de participantes y vehículos a competencias automotrices")
@RestController
@RequestMapping("/api/v1/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionService service;
    private EntityModel<InscripcionDTO> crearRecursoConLinks(InscripcionDTO dto) {
        EntityModel<InscripcionDTO> recurso = EntityModel.of(dto);
        Long id = dto.getId();
        recurso.add(linkTo(methodOn(InscripcionController.class).obtenerPorId(id)).withSelfRel());
        recurso.add(linkTo(methodOn(InscripcionController.class).listar()).withRel("all"));
        recurso.add(linkTo(methodOn(InscripcionController.class).actualizar(id, null)).withRel("update"));
        recurso.add(linkTo(methodOn(InscripcionController.class).eliminar(id)).withRel("delete"));
        return recurso;
    }

    @Operation(summary = "Listar todas las inscripciones", description = "Retorna la lista completa de inscripciones registradas")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente") })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<InscripcionDTO>>>> listar() {
        List<EntityModel<InscripcionDTO>> lista = service.listar().stream().map(this::toDTO).map(this::crearRecursoConLinks).collect(Collectors.toList());

        CollectionModel<EntityModel<InscripcionDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(InscripcionController.class).listar()).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<InscripcionDTO>>>builder().success(true).message("Listado").data(recurso).build());
    }

    @Operation(summary = "Obtener inscripcion por ID", description = "Retorna una inscripcion especifica por su identificador")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inscripcion encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Inscripcion no encontrada") })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<EntityModel<InscripcionDTO>>> obtenerPorId(
            @Parameter(description = "ID de la inscripcion", example = "1") @PathVariable Long id) {
        InscripcionDTO dto = toDTO(service.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.<EntityModel<InscripcionDTO>>builder().success(true).message("Encontrado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Crear inscripcion", description = "Crea una inscripcion para un evento. Valida que el evento exista via WebClient. Estado inicial: PENDIENTE")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Inscripcion creada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Evento no encontrado") })
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR')")
    public ResponseEntity<ApiResponse<EntityModel<InscripcionDTO>>> guardar(
            @Valid @RequestBody InscripcionDTO req,
            HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        Inscripcion nuevo = service.guardar(toEntity(req), bearer);
        InscripcionDTO dto = toDTO(nuevo);
        return ResponseEntity.status(201).body(ApiResponse.<EntityModel<InscripcionDTO>>builder()
                .success(true).message("Inscripción creada").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Actualizar inscripcion", description = "Actualiza los datos de una inscripcion existente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inscripcion actualizada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Inscripcion no encontrada") })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<InscripcionDTO>>> actualizar(
            @Parameter(description = "ID de la inscripcion a actualizar", example = "1") @PathVariable Long id,
            @Valid @RequestBody InscripcionDTO req) {
        Inscripcion actualizado = service.actualizar(id, toEntity(req));
        InscripcionDTO dto = toDTO(actualizado);
        return ResponseEntity.ok(ApiResponse.<EntityModel<InscripcionDTO>>builder().success(true).message("Actualizado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Eliminar inscripcion", description = "Elimina una inscripcion por su ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inscripcion eliminada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Inscripcion no encontrada") })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @Parameter(description = "ID de la inscripcion a eliminar", example = "1") @PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Eliminado").build());
    }

    @Operation(summary = "Inscripciones por evento", description = "Retorna todas las inscripciones asociadas a un evento")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inscripciones obtenidas") })
    @GetMapping("/evento/{eventoId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<InscripcionDTO>>>> porEvento(
            @Parameter(description = "ID del evento", example = "1") @PathVariable Long eventoId) {
        List<EntityModel<InscripcionDTO>> lista = service.obtenerPorEventoId(eventoId).stream()
                .map(this::toDTO).map(this::crearRecursoConLinks).collect(Collectors.toList());

        CollectionModel<EntityModel<InscripcionDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(InscripcionController.class).porEvento(eventoId)).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<InscripcionDTO>>>builder()
                .success(true).message("Inscripciones del evento " + eventoId).data(recurso).build());
    }

    @Operation(summary = "Inscripciones por vehículo", description = "Retorna todas las inscripciones de un vehiculo especifico")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inscripciones obtenidas") })
    @GetMapping("/vehiculo/{vehiculoId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<InscripcionDTO>>>> porVehiculo(
            @Parameter(description = "ID del vehículo", example = "1") @PathVariable Long vehiculoId) {
        List<EntityModel<InscripcionDTO>> lista = service.obtenerPorVehiculoId(vehiculoId).stream()
                .map(this::toDTO).map(this::crearRecursoConLinks).collect(Collectors.toList());

        CollectionModel<EntityModel<InscripcionDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(InscripcionController.class).porVehiculo(vehiculoId)).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<InscripcionDTO>>>builder()
                .success(true).message("Inscripciones del vehículo " + vehiculoId).data(recurso).build());
    }

    @Operation(summary = "Aprobar inscripcion", description = "Solo ADMIN: valida el vehículo via WebClient, cambia estado a APROBADA y notifica al participante")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inscripcion aprobada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "La inscripcion no esta en estado PENDIENTE"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Inscripcion no encontrada") })
    @PatchMapping("/{id}/aprobar")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<InscripcionDTO>>> aprobar(
            @Parameter(description = "ID de la inscripcion a aprobar", example = "1") @PathVariable Long id,
            HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        InscripcionDTO dto = toDTO(service.aprobar(id, bearer));
        return ResponseEntity.ok(ApiResponse.<EntityModel<InscripcionDTO>>builder()
                .success(true).message("Inscripción aprobada").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Rechazar inscripcion", description = "Solo ADMIN: cambia estado a RECHAZADA y notifica al participante")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inscripcion rechazada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "La inscripcion no esta en estado PENDIENTE"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Inscripcion no encontrada") })
    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<InscripcionDTO>>> rechazar(
            @Parameter(description = "ID de la inscripcion a rechazar", example = "1") @PathVariable Long id,
            HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        InscripcionDTO dto = toDTO(service.rechazar(id, bearer));
        return ResponseEntity.ok(ApiResponse.<EntityModel<InscripcionDTO>>builder()
                .success(true).message("Inscripción rechazada").data(crearRecursoConLinks(dto)).build());
    }

    private InscripcionDTO toDTO(Inscripcion e) {
        InscripcionDTO dto = new InscripcionDTO();
        dto.setId(e.getId());
        dto.setVehiculoId(e.getVehiculoId());
        dto.setEventoId(e.getEventoId());
        dto.setParticipante(e.getParticipante());
        dto.setCategoria(e.getCategoria());
        dto.setUsername(e.getUsername());
        dto.setEstado(e.getEstado());
        if (e.getRequisitos() != null) {
            dto.setRequisitos(e.getRequisitos().stream().map(r -> {
                RequisitoDTO rdto = new RequisitoDTO();
                rdto.setId(r.getId());
                rdto.setNombre(r.getNombre());
                rdto.setDescripcion(r.getDescripcion());
                return rdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Inscripcion toEntity(InscripcionDTO dto) {
        Inscripcion e = new Inscripcion();
        e.setVehiculoId(dto.getVehiculoId());
        e.setEventoId(dto.getEventoId());
        e.setParticipante(dto.getParticipante());
        e.setCategoria(dto.getCategoria());
        e.setUsername(dto.getUsername());
        e.setEstado(dto.getEstado() != null ? dto.getEstado() : "PENDIENTE");
        if (dto.getRequisitos() != null) {
            e.setRequisitos(dto.getRequisitos().stream().map(rdto -> {
                Requisito r = new Requisito();
                r.setNombre(rdto.getNombre());
                r.setDescripcion(rdto.getDescripcion());
                r.setInscripcion(e);
                return r;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
