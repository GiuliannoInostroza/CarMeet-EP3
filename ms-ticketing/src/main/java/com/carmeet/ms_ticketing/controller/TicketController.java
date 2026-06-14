package com.carmeet.ms_ticketing.controller;

import com.carmeet.ms_ticketing.dto.ApiResponse;
import com.carmeet.ms_ticketing.model.Ticket;
import com.carmeet.ms_ticketing.model.Beneficio;
import com.carmeet.ms_ticketing.dto.TicketDTO;
import com.carmeet.ms_ticketing.dto.BeneficioDTO;
import com.carmeet.ms_ticketing.service.TicketService;
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
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Tickets", description = "Venta y gestión del ciclo de vida de tickets de eventos")
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService service;

    private EntityModel<TicketDTO> crearRecursoConLinks(TicketDTO dto) {
        EntityModel<TicketDTO> recurso = EntityModel.of(dto);
        Long id = dto.getId();
        recurso.add(linkTo(methodOn(TicketController.class).obtenerPorId(id)).withSelfRel());
        recurso.add(linkTo(methodOn(TicketController.class).listar()).withRel("all"));
        recurso.add(linkTo(methodOn(TicketController.class).actualizar(id, null)).withRel("update"));
        recurso.add(linkTo(methodOn(TicketController.class).eliminar(id)).withRel("delete"));
        return recurso;
    }

    @Operation(summary = "Listar todos los tickets", description = "Retorna la lista completa de tickets registrados")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente") })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<TicketDTO>>>> listar() {
        List<EntityModel<TicketDTO>> lista = service.listar().stream().map(this::toDTO).map(this::crearRecursoConLinks).collect(Collectors.toList());

        CollectionModel<EntityModel<TicketDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(TicketController.class).listar()).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<TicketDTO>>>builder()
                .success(true).message("Listado").data(recurso).build());
    }

    @Operation(summary = "Obtener ticket por ID", description = "Retorna un ticket especifico por su identificador")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket no encontrado") })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<EntityModel<TicketDTO>>> obtenerPorId(
            @Parameter(description = "ID del ticket", example = "1") @PathVariable Long id) {
        TicketDTO dto = toDTO(service.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.<EntityModel<TicketDTO>>builder()
                .success(true).message("Encontrado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Crear ticket", description = "Crea un ticket para un evento. Valida que el evento exista via WebClient. Estado inicial: PENDIENTE")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Ticket creado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Evento no encontrado") })
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<EntityModel<TicketDTO>>> guardar(
            @Valid @RequestBody TicketDTO req,
            HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        Ticket nuevo = service.guardar(toEntity(req), bearer);
        TicketDTO dto = toDTO(nuevo);
        return ResponseEntity.status(201).body(ApiResponse.<EntityModel<TicketDTO>>builder()
                .success(true).message("Ticket creado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Actualizar ticket", description = "Actualiza los datos de un ticket existente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket actualizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket no encontrado") })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<TicketDTO>>> actualizar(
            @Parameter(description = "ID del ticket a actualizar", example = "1") @PathVariable Long id,
            @Valid @RequestBody TicketDTO req) {
        Ticket actualizado = service.actualizar(id, toEntity(req));
        TicketDTO dto = toDTO(actualizado);
        return ResponseEntity.ok(ApiResponse.<EntityModel<TicketDTO>>builder()
                .success(true).message("Actualizado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Eliminar ticket", description = "Elimina un ticket por su ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket eliminado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket no encontrado") })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @Parameter(description = "ID del ticket a eliminar", example = "1") @PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Eliminado").build());
    }

    @Operation(summary = "Tickets por evento", description = "Retorna todos los tickets asociados a un evento especifico")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tickets obtenidos") })
    @GetMapping("/evento/{eventoId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<TicketDTO>>>> porEvento(
            @Parameter(description = "ID del evento", example = "1") @PathVariable Long eventoId) {
        List<EntityModel<TicketDTO>> lista = service.obtenerPorEventoId(eventoId).stream()
                .map(this::toDTO).map(this::crearRecursoConLinks).collect(Collectors.toList());

        CollectionModel<EntityModel<TicketDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(TicketController.class).porEvento(eventoId)).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<TicketDTO>>>builder()
                .success(true).message("Tickets del evento " + eventoId).data(recurso).build());
    }

    @Operation(summary = "Tickets por usuario", description = "Retorna todos los tickets comprados por un usuario especifico")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tickets obtenidos") })
    @GetMapping("/usuario/{username}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<TicketDTO>>>> porUsuario(
            @Parameter(description = "Username del usuario", example = "juanito99") @PathVariable String username) {
        List<EntityModel<TicketDTO>> lista = service.obtenerPorUsername(username).stream()
                .map(this::toDTO).map(this::crearRecursoConLinks).collect(Collectors.toList());

        CollectionModel<EntityModel<TicketDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(TicketController.class).porUsuario(username)).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<TicketDTO>>>builder()
                .success(true).message("Tickets del usuario: " + username).data(recurso).build());
    }

    @Operation(summary = "Cancelar ticket", description = "Cambia el estado del ticket a CANCELADO. Solo aplica si el estado es PENDIENTE")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket cancelado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "El ticket no esta en estado PENDIENTE"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket no encontrado") })
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<EntityModel<TicketDTO>>> cancelar(
            @Parameter(description = "ID del ticket a cancelar", example = "1") @PathVariable Long id) {
        TicketDTO dto = toDTO(service.cancelar(id));
        return ResponseEntity.ok(ApiResponse.<EntityModel<TicketDTO>>builder()
                .success(true).message("Ticket cancelado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Pagar ticket", description = "Procesa el pago del ticket via ms-payment-mock. Si es aprobado, cambia estado a PAGADO y envia notificacion")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pago procesado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "402", description = "Pago rechazado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "El ticket no esta en estado PENDIENTE"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket no encontrado") })
    @PatchMapping("/{id}/pagar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<EntityModel<TicketDTO>>> pagar(
            @Parameter(description = "ID del ticket a pagar", example = "1") @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        String metodoPago = (body != null) ? body.get("metodoPago") : "TARJETA";
        TicketDTO dto = toDTO(service.pagar(id, metodoPago, bearer));
        return ResponseEntity.ok(ApiResponse.<EntityModel<TicketDTO>>builder()
                .success(true).message("Pago procesado exitosamente").data(crearRecursoConLinks(dto)).build());
    }

    private TicketDTO toDTO(Ticket e) {
        TicketDTO dto = new TicketDTO();
        dto.setId(e.getId());
        dto.setEventoId(e.getEventoId());
        dto.setPrecio(e.getPrecio());
        dto.setCategoria(e.getCategoria());
        dto.setEstado(e.getEstado());
        dto.setUsername(e.getUsername());
        if (e.getBeneficios() != null) {
            dto.setBeneficios(e.getBeneficios().stream().map(p -> {
                BeneficioDTO pdto = new BeneficioDTO();
                pdto.setId(p.getId());
                pdto.setNombre(p.getNombre());
                pdto.setDescripcion(p.getDescripcion());
                return pdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Ticket toEntity(TicketDTO dto) {
        Ticket e = new Ticket();
        e.setEventoId(dto.getEventoId());
        e.setPrecio(dto.getPrecio());
        e.setCategoria(dto.getCategoria());
        e.setEstado(dto.getEstado() != null ? dto.getEstado() : "PENDIENTE");
        e.setUsername(dto.getUsername());
        if (dto.getBeneficios() != null) {
            e.setBeneficios(dto.getBeneficios().stream().map(pdto -> {
                Beneficio p = new Beneficio();
                p.setNombre(pdto.getNombre());
                p.setDescripcion(pdto.getDescripcion());
                p.setTicket(e);
                return p;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
