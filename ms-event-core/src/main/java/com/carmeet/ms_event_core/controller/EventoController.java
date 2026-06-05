package com.carmeet.ms_event_core.controller;

import com.carmeet.ms_event_core.dto.ApiResponse;
import com.carmeet.ms_event_core.model.Evento;
import com.carmeet.ms_event_core.model.Patrocinador;
import com.carmeet.ms_event_core.dto.EventoDTO;
import com.carmeet.ms_event_core.dto.PatrocinadorDTO;
import com.carmeet.ms_event_core.service.EventoService;
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

@Tag(name = "Eventos", description = "Gestión de eventos automotrices y sus patrocinadores")
@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService service;

    private EntityModel<EventoDTO> crearRecursoConLinks(EventoDTO dto) {
        EntityModel<EventoDTO> recurso = EntityModel.of(dto);
        Long id = dto.getId();
        recurso.add(linkTo(methodOn(EventoController.class).obtenerPorId(id)).withSelfRel());
        recurso.add(linkTo(methodOn(EventoController.class).listar()).withRel("all"));
        recurso.add(linkTo(methodOn(EventoController.class).actualizar(id, null)).withRel("update"));
        recurso.add(linkTo(methodOn(EventoController.class).eliminar(id)).withRel("delete"));
        return recurso;
    }

    @Operation(summary = "Listar todos los eventos", description = "Retorna la lista completa de eventos registrados en el sistema")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente") })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<EventoDTO>>>> listar() {
        List<EntityModel<EventoDTO>> lista = service.listar().stream().map(this::toDTO).map(this::crearRecursoConLinks).collect(Collectors.toList());
        
        CollectionModel<EntityModel<EventoDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(EventoController.class).listar()).withSelfRel());
                
        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<EventoDTO>>>builder().success(true).message("Listado").data(recurso).build());
    }

    @Operation(summary = "Obtener evento por ID", description = "Retorna un evento especifico por su identificador")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Evento no encontrado") })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<EntityModel<EventoDTO>>> obtenerPorId(
            @Parameter(description = "ID del evento", example = "1") @PathVariable Long id) {
        EventoDTO dto = toDTO(service.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.<EntityModel<EventoDTO>>builder().success(true).message("Encontrado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Crear evento", description = "Crea un nuevo evento con sus patrocinadores opcionales")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Evento creado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos") })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<EventoDTO>>> guardar(@Valid @RequestBody EventoDTO req) {
        Evento nuevo = service.guardar(toEntity(req));
        EventoDTO dto = toDTO(nuevo);
        return ResponseEntity.status(201).body(ApiResponse.<EntityModel<EventoDTO>>builder().success(true).message("Creado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Actualizar evento", description = "Actualiza los datos de un evento existente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Evento actualizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Evento no encontrado") })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<EventoDTO>>> actualizar(
            @Parameter(description = "ID del evento a actualizar", example = "1") @PathVariable Long id,
            @Valid @RequestBody EventoDTO req) {
        Evento actualizado = service.actualizar(id, toEntity(req));
        EventoDTO dto = toDTO(actualizado);
        return ResponseEntity.ok(ApiResponse.<EntityModel<EventoDTO>>builder().success(true).message("Actualizado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Eliminar evento", description = "Elimina un evento por su ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Evento eliminado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Evento no encontrado") })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @Parameter(description = "ID del evento a eliminar", example = "1") @PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Eliminado").build());
    }

    @Operation(summary = "Listar patrocinadores de un evento", description = "Retorna todos los patrocinadores asociados a un evento dado")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patrocinadores obtenidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Evento no encontrado") })
    @GetMapping("/{id}/patrocinadores")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<PatrocinadorDTO>>>> listarPatrocinadores(
            @Parameter(description = "ID del evento", example = "1") @PathVariable Long id) {
        Evento evento = service.obtenerPorId(id);
        List<EntityModel<PatrocinadorDTO>> lista = evento.getPatrocinadores().stream()
                .map(p -> {
                    PatrocinadorDTO dto = new PatrocinadorDTO();
                    dto.setId(p.getId());
                    dto.setNombre(p.getNombre());
                    dto.setNivel(p.getNivel());
                    return EntityModel.of(dto, 
                            linkTo(methodOn(EventoController.class).listarPatrocinadores(id)).withRel("patrocinadores"),
                            linkTo(methodOn(EventoController.class).obtenerPorId(id)).withRel("evento"));
                }).collect(Collectors.toList());
                
        CollectionModel<EntityModel<PatrocinadorDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(EventoController.class).listarPatrocinadores(id)).withSelfRel(),
                linkTo(methodOn(EventoController.class).obtenerPorId(id)).withRel("evento"));
                
        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<PatrocinadorDTO>>>builder()
                .success(true).message("Patrocinadores del evento " + id).data(recurso).build());
    }

    @Operation(summary = "Listar eventos proximos", description = "Retorna eventos cuya fecha es posterior a la fecha actual")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Eventos proximos obtenidos") })
    @GetMapping("/proximos")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<EventoDTO>>>> listarProximos() {
        List<EntityModel<EventoDTO>> lista = service.listarProximos().stream().map(this::toDTO).map(this::crearRecursoConLinks).collect(Collectors.toList());
        
        CollectionModel<EntityModel<EventoDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(EventoController.class).listarProximos()).withSelfRel());
                
        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<EventoDTO>>>builder()
                .success(true).message("Eventos próximos").data(recurso).build());
    }

    @Operation(summary = "Buscar eventos por nombre", description = "Busca eventos cuyo nombre contenga el texto indicado (busqueda parcial)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Resultados obtenidos") })
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<EventoDTO>>>> buscarPorNombre(
            @Parameter(description = "Texto a buscar en el nombre del evento", example = "Rally") @RequestParam String nombre) {
        List<EntityModel<EventoDTO>> lista = service.buscarPorNombre(nombre).stream().map(this::toDTO).map(this::crearRecursoConLinks).collect(Collectors.toList());
        
        CollectionModel<EntityModel<EventoDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(EventoController.class).buscarPorNombre(nombre)).withSelfRel());
                
        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<EventoDTO>>>builder()
                .success(true).message("Resultados para: " + nombre).data(recurso).build());
    }

    private EventoDTO toDTO(Evento e) {
        EventoDTO dto = new EventoDTO();
        dto.setId(e.getId());
        dto.setNombre(e.getNombre());
        dto.setFecha(e.getFecha());
        dto.setUbicacion(e.getUbicacion());
        if (e.getPatrocinadores() != null) {
            dto.setPatrocinadores(e.getPatrocinadores().stream().map(p -> {
                PatrocinadorDTO pdto = new PatrocinadorDTO();
                pdto.setId(p.getId());
                pdto.setNombre(p.getNombre());
                pdto.setNivel(p.getNivel());
                return pdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Evento toEntity(EventoDTO dto) {
        Evento e = new Evento();
        e.setNombre(dto.getNombre());
        e.setFecha(dto.getFecha());
        e.setUbicacion(dto.getUbicacion());
        if (dto.getPatrocinadores() != null) {
            e.setPatrocinadores(dto.getPatrocinadores().stream().map(pdto -> {
                Patrocinador p = new Patrocinador();
                p.setNombre(pdto.getNombre());
                p.setNivel(pdto.getNivel());
                p.setEvento(e);
                return p;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
