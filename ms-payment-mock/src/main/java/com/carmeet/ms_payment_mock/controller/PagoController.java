package com.carmeet.ms_payment_mock.controller;

import com.carmeet.ms_payment_mock.dto.ApiResponse;
import com.carmeet.ms_payment_mock.model.Pago;
import com.carmeet.ms_payment_mock.model.TransaccionLog;
import com.carmeet.ms_payment_mock.dto.PagoDTO;
import com.carmeet.ms_payment_mock.dto.TransaccionLogDTO;
import com.carmeet.ms_payment_mock.service.PagoService;
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

@Tag(name = "Pagos", description = "Mock de pasarela de pagos y registro de transacciones")
@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService service;

    private EntityModel<PagoDTO> crearRecursoConLinks(PagoDTO dto) {
        EntityModel<PagoDTO> recurso = EntityModel.of(dto);
        Long id = dto.getId();
        recurso.add(linkTo(methodOn(PagoController.class).obtenerPorId(id)).withSelfRel());
        recurso.add(linkTo(methodOn(PagoController.class).listar()).withRel("all"));
        recurso.add(linkTo(methodOn(PagoController.class).actualizar(id, null)).withRel("update"));
        recurso.add(linkTo(methodOn(PagoController.class).eliminar(id)).withRel("delete"));
        return recurso;
    }

    @Operation(summary = "Listar todos los pagos", description = "Retorna la lista completa de pagos registrados")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente") })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<PagoDTO>>>> listar() {
        List<EntityModel<PagoDTO>> lista = service.listar().stream().map(this::toDTO).map(this::crearRecursoConLinks).collect(Collectors.toList());

        CollectionModel<EntityModel<PagoDTO>> recurso = CollectionModel.of(lista,
                linkTo(methodOn(PagoController.class).listar()).withSelfRel());

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<PagoDTO>>>builder().success(true).message("Listado").data(recurso).build());
    }

    @Operation(summary = "Obtener pago por ID", description = "Retorna un pago especifico por su identificador")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pago no encontrado") })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<EntityModel<PagoDTO>>> obtenerPorId(
            @Parameter(description = "ID del pago", example = "1") @PathVariable Long id) {
        PagoDTO dto = toDTO(service.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.<EntityModel<PagoDTO>>builder().success(true).message("Encontrado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Registrar pago manualmente", description = "Crea un registro de pago directamente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Pago creado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos") })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<PagoDTO>>> guardar(@Valid @RequestBody PagoDTO req) {
        Pago nuevo = service.guardar(toEntity(req));
        PagoDTO dto = toDTO(nuevo);
        return ResponseEntity.status(201).body(ApiResponse.<EntityModel<PagoDTO>>builder().success(true).message("Creado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Actualizar pago", description = "Actualiza los datos de un pago existente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pago actualizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pago no encontrado") })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<PagoDTO>>> actualizar(
            @Parameter(description = "ID del pago a actualizar", example = "1") @PathVariable Long id,
            @Valid @RequestBody PagoDTO req) {
        Pago actualizado = service.actualizar(id, toEntity(req));
        PagoDTO dto = toDTO(actualizado);
        return ResponseEntity.ok(ApiResponse.<EntityModel<PagoDTO>>builder().success(true).message("Actualizado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Eliminar pago", description = "Elimina un pago por su ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pago eliminado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pago no encontrado") })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @Parameter(description = "ID del pago a eliminar", example = "1") @PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Eliminado").build());
    }

    @Operation(summary = "Obtener pago por Ticket ID", description = "Busca el registro de pago asociado a un ticket especifico")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No existe pago para ese ticket") })
    @GetMapping("/ticket/{ticketId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<EntityModel<PagoDTO>>> obtenerPorTicket(
            @Parameter(description = "ID del ticket", example = "1") @PathVariable Long ticketId) {
        PagoDTO dto = toDTO(service.obtenerPorTicketId(ticketId));
        return ResponseEntity.ok(ApiResponse.<EntityModel<PagoDTO>>builder()
                .success(true).message("Pago del ticket " + ticketId).data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Procesar pago (Mock)", description = "Simula la validacion y procesamiento de un pago en una pasarela externa")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pago procesado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos") })
    @PostMapping("/procesar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<EntityModel<PagoDTO>>> procesarPago(@Valid @RequestBody PagoDTO req) {
        Pago resultado = service.procesarPago(toEntity(req));
        PagoDTO dto = toDTO(resultado);
        return ResponseEntity.ok(ApiResponse.<EntityModel<PagoDTO>>builder()
                .success(true).message("Pago procesado").data(crearRecursoConLinks(dto)).build());
    }

    @Operation(summary = "Obtener logs de transaccion", description = "Retorna el historial de estados de un pago especifico")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logs obtenidos") })
    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPETIDOR', 'ROLE_ESPECTADOR')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<TransaccionLogDTO>>>> obtenerLogs(
            @Parameter(description = "ID del pago", example = "1") @PathVariable Long id) {
        List<EntityModel<TransaccionLogDTO>> logs = service.obtenerLogs(id).stream()
                .map(l -> {
                    TransaccionLogDTO dto2 = new TransaccionLogDTO();
                    dto2.setId(l.getId());
                    dto2.setEstado(l.getEstado());
                    return EntityModel.of(dto2, 
                            linkTo(methodOn(PagoController.class).obtenerLogs(id)).withRel("logs"),
                            linkTo(methodOn(PagoController.class).obtenerPorId(id)).withRel("pago"));
                }).collect(Collectors.toList());

        CollectionModel<EntityModel<TransaccionLogDTO>> recurso = CollectionModel.of(logs,
                linkTo(methodOn(PagoController.class).obtenerLogs(id)).withSelfRel(),
                linkTo(methodOn(PagoController.class).obtenerPorId(id)).withRel("pago"));

        return ResponseEntity.ok(ApiResponse.<CollectionModel<EntityModel<TransaccionLogDTO>>>builder()
                .success(true).message("Logs del pago " + id).data(recurso).build());
    }

    private PagoDTO toDTO(Pago e) {
        PagoDTO dto = new PagoDTO();
        dto.setId(e.getId());
        dto.setTicketId(e.getTicketId());
        dto.setMonto(e.getMonto());
        dto.setMetodoPago(e.getMetodoPago());
        if (e.getLogs() != null) {
            dto.setLogs(e.getLogs().stream().map(p -> {
                TransaccionLogDTO pdto = new TransaccionLogDTO();
                pdto.setId(p.getId());
                pdto.setEstado(p.getEstado());
                return pdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Pago toEntity(PagoDTO dto) {
        Pago e = new Pago();
        e.setTicketId(dto.getTicketId());
        e.setMonto(dto.getMonto());
        e.setMetodoPago(dto.getMetodoPago());
        if (dto.getLogs() != null) {
            e.setLogs(dto.getLogs().stream().map(pdto -> {
                TransaccionLog p = new TransaccionLog();
                p.setEstado(pdto.getEstado());
                p.setFecha(java.time.LocalDateTime.now());
                p.setPago(e);
                return p;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
