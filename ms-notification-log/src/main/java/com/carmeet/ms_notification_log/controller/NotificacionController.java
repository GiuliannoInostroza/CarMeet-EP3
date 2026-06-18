package com.carmeet.ms_notification_log.controller;

import com.carmeet.ms_notification_log.dto.ApiResponse;
import com.carmeet.ms_notification_log.model.Notificacion;
import com.carmeet.ms_notification_log.model.Adjunto;
import com.carmeet.ms_notification_log.dto.NotificacionDTO;
import com.carmeet.ms_notification_log.dto.AdjuntoDTO;
import com.carmeet.ms_notification_log.service.NotificacionService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Notificaciones", description = "Envio y registro de notificaciones a usuarios")
@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService service;

    private EntityModel<NotificacionDTO> crearRecursoConLinks(NotificacionDTO dto) {
        EntityModel<NotificacionDTO> recurso = EntityModel.of(dto);
        Long id = dto.getId();
        recurso.add(linkTo(methodOn(NotificacionController.class).obtenerPorId(id)).withSelfRel());
        recurso.add(linkTo(methodOn(NotificacionController.class).listar()).withRel("all"));
        recurso.add(linkTo(methodOn(NotificacionController.class).actualizar(id, null)).withRel("update"));
        recurso.add(linkTo(methodOn(NotificacionController.class).eliminar(id)).withRel("delete"));
        return recurso;
    }

    @Operation(summary = "Listar todas las notificaciones", description = "Retorna la lista completa de notificaciones registradas")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<NotificacionDTO>>>> listar() {
        List<EntityModel<NotificacionDTO>> lista = service.listar().stream().map(this::toDTO).map(this::crearRecursoConLinks).collect(Collectors.toList());

        CollectionModel<EntityModel<NotificacionDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(NotificacionController.class).listar()).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<NotificacionDTO>>>builder().success(true).message("Listado").data(recurso).build());
    }

    @Operation(summary = "Obtener notificacion por ID", description = "Retorna una notificacion especifica por su identificador")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notificacion encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notificacion no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<EntityModel<NotificacionDTO>>> obtenerPorId(
            @Parameter(description = "ID de la notificacion", example = "1") @PathVariable Long id) {
        NotificacionDTO dto = toDTO(service.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.<EntityModel<NotificacionDTO>>builder().success(true).message("Encontrado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Crear notificacion directamente", description = "Registra una notificacion sin simular proceso de envio")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Notificacion creada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<NotificacionDTO>>> guardar(@Valid @RequestBody NotificacionDTO req) {
        Notificacion nuevo = service.guardar(toEntity(req));
        NotificacionDTO dto = toDTO(nuevo);
        return ResponseEntity.status(201).body(ApiResponse.<EntityModel<NotificacionDTO>>builder().success(true).message("Creado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Actualizar notificacion", description = "Actualiza los datos de una notificacion existente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notificacion actualizada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notificacion no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<NotificacionDTO>>> actualizar(
            @Parameter(description = "ID de la notificacion a actualizar", example = "1") @PathVariable Long id,
            @Valid @RequestBody NotificacionDTO req) {
        Notificacion actualizado = service.actualizar(id, toEntity(req));
        NotificacionDTO dto = toDTO(actualizado);
        return ResponseEntity.ok(ApiResponse.<EntityModel<NotificacionDTO>>builder().success(true).message("Actualizado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Eliminar notificacion", description = "Elimina una notificacion por su ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notificacion eliminado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notificacion no encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @Parameter(description = "ID de la notificacion a eliminar", example = "1") @PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Eliminado").build());
    }

    @Operation(summary = "Notificaciones por destinatario", description = "Retorna todas las notificaciones enviadas a un usuario especifico")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notificaciones obtenidas"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
    @GetMapping("/destinatario/{username}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<NotificacionDTO>>>> obtenerPorDestinatario(
            @Parameter(description = "Username del destinatario", example = "juanito99") @PathVariable String username) {
        List<EntityModel<NotificacionDTO>> lista = service.obtenerPorDestinatario(username)
                .stream().map(this::toDTO).map(dto -> {
                    return EntityModel.of(dto,
                            linkTo(methodOn(NotificacionController.class).obtenerPorId(dto.getId())).withSelfRel(),
                            linkTo(methodOn(NotificacionController.class).obtenerPorDestinatario(username)).withRel("destinatario_notificaciones"));
                }).collect(Collectors.toList());

        CollectionModel<EntityModel<NotificacionDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(NotificacionController.class).obtenerPorDestinatario(username)).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<NotificacionDTO>>>builder()
                .success(true).message("Notificaciones de: " + username).data(recurso).build());
    }

    @Operation(summary = "Enviar notificacion", description = "Simula el envio de la notificacion y la guarda en base de datos")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Notificacion enviada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
    @PostMapping("/enviar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<EntityModel<NotificacionDTO>>> enviar(@Valid @RequestBody NotificacionDTO req) {
        Notificacion enviada = service.enviar(toEntity(req));
        NotificacionDTO dto = toDTO(enviada);
        EntityModel<NotificacionDTO> recurso = EntityModel.of(dto);
        recurso.add(linkTo(methodOn(NotificacionController.class).obtenerPorId(dto.getId())).withSelfRel());

        return ResponseEntity.status(201).body(ApiResponse.<EntityModel<NotificacionDTO>>builder()
                .success(true).message("Notificación enviada").data(recurso).build());
    }

    @Operation(summary = "Marcar notificacion como leida", description = "Cambia el estado de una notificacion especifica a leida (true)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notificacion marcada como leida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notificacion no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
    @PatchMapping("/{id}/marcar-leida")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<EntityModel<NotificacionDTO>>> marcarLeida(
            @Parameter(description = "ID de la notificacion", example = "1") @PathVariable Long id) {
        NotificacionDTO dto = toDTO(service.marcarLeida(id));
        EntityModel<NotificacionDTO> recurso = EntityModel.of(dto);
        recurso.add(linkTo(methodOn(NotificacionController.class).obtenerPorId(id)).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<EntityModel<NotificacionDTO>>builder()
                .success(true).message("Notificación marcada como leída").data(recurso).build());
    }

    private NotificacionDTO toDTO(Notificacion e) {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setId(e.getId());
        dto.setDestinatario(e.getDestinatario());
        dto.setMensaje(e.getMensaje());
        dto.setLeida(e.getLeida());
        if (e.getAdjuntos() != null) {
            dto.setAdjuntos(e.getAdjuntos().stream().map(p -> {
                AdjuntoDTO pdto = new AdjuntoDTO();
                pdto.setId(p.getId());
                pdto.setNombreArchivo(p.getNombreArchivo());
                pdto.setUrl(p.getUrl());
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
        e.setLeida(dto.getLeida() != null ? dto.getLeida() : false);
        if (dto.getAdjuntos() != null) {
            e.setAdjuntos(dto.getAdjuntos().stream().map(pdto -> {
                Adjunto p = new Adjunto();
                p.setNombreArchivo(pdto.getNombreArchivo());
                p.setUrl(pdto.getUrl());
                p.setRutaArchivo(pdto.getRutaArchivo());
                p.setNotificacion(e);
                return p;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
