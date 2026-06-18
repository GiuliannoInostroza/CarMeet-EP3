package com.carmeet.ms_live_scoreboard.controller;

import com.carmeet.ms_live_scoreboard.dto.ApiResponse;
import com.carmeet.ms_live_scoreboard.model.Puntuacion;
import com.carmeet.ms_live_scoreboard.model.DetallePuntuacion;
import com.carmeet.ms_live_scoreboard.dto.PuntuacionDTO;
import com.carmeet.ms_live_scoreboard.dto.DetallePuntuacionDTO;
import com.carmeet.ms_live_scoreboard.service.PuntuacionService;
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

@Tag(name = "Puntuaciones", description = "Tabla de puntuaciones en tiempo real de competencias automotrices")
@RestController
@RequestMapping("/api/v1/puntuaciones")
@RequiredArgsConstructor
public class PuntuacionController {

    private final PuntuacionService service;

    private EntityModel<PuntuacionDTO> crearRecursoConLinks(PuntuacionDTO dto) {
        EntityModel<PuntuacionDTO> recurso = EntityModel.of(dto);
        Long id = dto.getId();
        recurso.add(linkTo(methodOn(PuntuacionController.class).obtenerPorId(id)).withSelfRel());
        recurso.add(linkTo(methodOn(PuntuacionController.class).listar()).withRel("all"));
        recurso.add(linkTo(methodOn(PuntuacionController.class).actualizar(id, null)).withRel("update"));
        recurso.add(linkTo(methodOn(PuntuacionController.class).eliminar(id)).withRel("delete"));
        return recurso;
    }

    @Operation(summary = "Listar todas las puntuaciones", description = "Retorna la lista completa de puntuaciones registradas")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<PuntuacionDTO>>>> listar() {
        List<EntityModel<PuntuacionDTO>> lista = service.listar().stream().map(this::toDTO).map(this::crearRecursoConLinks).collect(Collectors.toList());

        CollectionModel<EntityModel<PuntuacionDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(PuntuacionController.class).listar()).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<PuntuacionDTO>>>builder().success(true).message("Listado").data(recurso).build());
    }

    @Operation(summary = "Obtener puntuacion por ID", description = "Retorna una puntuacion especifica por su identificador")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Puntuacion encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Puntuacion no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<EntityModel<PuntuacionDTO>>> obtenerPorId(
            @Parameter(description = "ID de la puntuacion", example = "1") @PathVariable Long id) {
        PuntuacionDTO dto = toDTO(service.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.<EntityModel<PuntuacionDTO>>builder().success(true).message("Encontrado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Registrar puntuacion", description = "Registra la puntuacion de una inscripcion. Valida que la inscripcion exista via WebClient")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Puntuacion registrada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Inscripcion no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<PuntuacionDTO>>> guardar(
            @Valid @RequestBody PuntuacionDTO req,
            HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        Puntuacion nuevo = service.guardar(toEntity(req), bearer);
        PuntuacionDTO dto = toDTO(nuevo);
        return ResponseEntity.status(201).body(ApiResponse.<EntityModel<PuntuacionDTO>>builder()
                .success(true).message("Puntuación registrada").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Actualizar puntuacion", description = "Actualiza los datos de una puntuacion existente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Puntuacion actualizada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Puntuacion no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<PuntuacionDTO>>> actualizar(
            @Parameter(description = "ID de la puntuacion a actualizar", example = "1") @PathVariable Long id,
            @Valid @RequestBody PuntuacionDTO req) {
        Puntuacion actualizado = service.actualizar(id, toEntity(req));
        PuntuacionDTO dto = toDTO(actualizado);
        return ResponseEntity.ok(ApiResponse.<EntityModel<PuntuacionDTO>>builder().success(true).message("Actualizado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Eliminar puntuacion", description = "Elimina una puntuacion por su ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Puntuacion eliminada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Puntuacion no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @Parameter(description = "ID de la puntuacion a eliminar", example = "1") @PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Eliminado").build());
    }

    @Operation(summary = "Ranking de un evento", description = "Retorna las puntuaciones de un evento ordenadas de mayor a menor")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ranking obtenido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
    @GetMapping("/evento/{eventoId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<PuntuacionDTO>>>> rankingEvento(
            @Parameter(description = "ID del evento", example = "1") @PathVariable Long eventoId) {
        List<EntityModel<PuntuacionDTO>> lista = service.rankingPorEvento(eventoId).stream()
                .map(this::toDTO).map(this::crearRecursoConLinks).collect(Collectors.toList());

        CollectionModel<EntityModel<PuntuacionDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(PuntuacionController.class).rankingEvento(eventoId)).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<PuntuacionDTO>>>builder()
                .success(true).message("Ranking del evento " + eventoId).data(recurso).build());
    }

    @Operation(summary = "Puntuaciones por inscripcion", description = "Retorna todas las puntuaciones de una inscripcion especifica")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Puntuaciones obtenidas"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
    @GetMapping("/inscripcion/{inscripcionId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<PuntuacionDTO>>>> porInscripcion(
            @Parameter(description = "ID de la inscripcion", example = "1") @PathVariable Long inscripcionId) {
        List<EntityModel<PuntuacionDTO>> lista = service.porInscripcion(inscripcionId).stream()
                .map(this::toDTO).map(this::crearRecursoConLinks).collect(Collectors.toList());

        CollectionModel<EntityModel<PuntuacionDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(PuntuacionController.class).porInscripcion(inscripcionId)).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<PuntuacionDTO>>>builder()
                .success(true).message("Puntuaciones de inscripción " + inscripcionId).data(recurso).build());
    }

    @Operation(summary = "Top 10 global", description = "Retorna las 10 puntuaciones mas altas de todas las competencias")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Top 10 obtenido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
    @GetMapping("/ranking")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<PuntuacionDTO>>>> top10() {
        List<EntityModel<PuntuacionDTO>> lista = service.top10Global().stream()
                .map(this::toDTO).map(this::crearRecursoConLinks).collect(Collectors.toList());

        CollectionModel<EntityModel<PuntuacionDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(PuntuacionController.class).top10()).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<PuntuacionDTO>>>builder()
                .success(true).message("Top 10 puntuaciones globales").data(recurso).build());
    }

    private PuntuacionDTO toDTO(Puntuacion e) {
        PuntuacionDTO dto = new PuntuacionDTO();
        dto.setId(e.getId());
        dto.setInscripcionId(e.getInscripcionId());
        dto.setEventoId(e.getEventoId());
        dto.setPuntos(e.getPuntos());
        if (e.getDetalles() != null) {
            dto.setDetalles(e.getDetalles().stream().map(p -> {
                DetallePuntuacionDTO pdto = new DetallePuntuacionDTO();
                pdto.setId(p.getId());
                pdto.setCategoria(p.getCategoria());
                pdto.setPuntosAsignados(p.getPuntosAsignados());
                pdto.setDescripcion(p.getDescripcion());
                return pdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Puntuacion toEntity(PuntuacionDTO dto) {
        Puntuacion e = new Puntuacion();
        e.setInscripcionId(dto.getInscripcionId());
        e.setEventoId(dto.getEventoId());
        e.setPuntos(dto.getPuntos());
        if (dto.getDetalles() != null) {
            e.setDetalles(dto.getDetalles().stream().map(pdto -> {
                DetallePuntuacion p = new DetallePuntuacion();
                p.setCategoria(pdto.getCategoria());
                p.setPuntosAsignados(pdto.getPuntosAsignados());
                p.setDescripcion(pdto.getDescripcion());
                p.setPuntuacion(e);
                return p;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
