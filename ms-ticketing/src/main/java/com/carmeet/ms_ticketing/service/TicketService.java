package com.carmeet.ms_ticketing.service;

import com.carmeet.ms_ticketing.model.Ticket;
import com.carmeet.ms_ticketing.repository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TicketService {

    private final TicketRepository repo;
    private final WebClient.Builder webClientBuilder;

    @Value("${services.event-core.base-url}")
    private String eventCoreUrl;

    @Value("${services.payment-mock.base-url}")
    private String paymentMockUrl;

    @Value("${services.notification-log.base-url}")
    private String notificationLogUrl;

    public TicketService(TicketRepository repo, WebClient.Builder webClientBuilder) {
        this.repo = repo;
        this.webClientBuilder = webClientBuilder;
    }

    public List<Ticket> listar() {
        return repo.findAll();
    }

    public Ticket obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con id: " + id));
    }

    /**
     * Crea un ticket. Valida que el evento exista via WebClient.
     * Estado inicial: PENDIENTE
     */
    public Ticket guardar(Ticket ticket, String bearerToken) {
        // Validar que el evento existe en ms-event-core
        validarEvento(ticket.getEventoId(), bearerToken);

        if (ticket.getBeneficios() != null) {
            ticket.getBeneficios().forEach(b -> b.setTicket(ticket));
        }
        ticket.setEstado("PENDIENTE");
        return repo.save(ticket);
    }

    public Ticket actualizar(Long id, Ticket datosNuevos) {
        Ticket existente = obtenerPorId(id);
        existente.setEventoId(datosNuevos.getEventoId());
        existente.setPrecio(datosNuevos.getPrecio());
        existente.setCategoria(datosNuevos.getCategoria());
        existente.setEstado(datosNuevos.getEstado());
        existente.setUsername(datosNuevos.getUsername());

        existente.getBeneficios().clear();
        if (datosNuevos.getBeneficios() != null) {
            datosNuevos.getBeneficios().forEach(b -> {
                b.setTicket(existente);
                existente.getBeneficios().add(b);
            });
        }

        return repo.save(existente);
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Ticket no encontrado con id: " + id);
        }
        repo.deleteById(id);
    }

    /** Lista los tickets de un evento */
    public List<Ticket> obtenerPorEventoId(Long eventoId) {
        return repo.findByEventoId(eventoId);
    }

    /** Lista los tickets de un usuario */
    public List<Ticket> obtenerPorUsername(String username) {
        return repo.findByUsername(username);
    }

    /**
     * Cancela un ticket. Solo se puede cancelar si está en PENDIENTE.
     */
    public Ticket cancelar(Long id) {
        Ticket ticket = obtenerPorId(id);
        if (!"PENDIENTE".equals(ticket.getEstado())) {
            throw new RuntimeException("Solo se pueden cancelar tickets en estado PENDIENTE. Estado actual: " + ticket.getEstado());
        }
        ticket.setEstado("CANCELADO");
        return repo.save(ticket);
    }

    /**
     * Procesa el pago de un ticket:
     * 1. Valida que el ticket esté en PENDIENTE.
     * 2. Llama a ms-payment-mock para procesar el pago.
     * 3. Si APROBADO: cambia estado a PAGADO.
     * 4. Notifica al usuario via ms-notification-log.
     */
    public Ticket pagar(Long id, String metodoPago, String bearerToken) {
        Ticket ticket = obtenerPorId(id);
        if (!"PENDIENTE".equals(ticket.getEstado())) {
            throw new RuntimeException("Solo se pueden pagar tickets en estado PENDIENTE. Estado actual: " + ticket.getEstado());
        }

        // Llamar a ms-payment-mock para procesar el pago
        Map<String, Object> pagoBody = Map.of(
                "ticketId", ticket.getId(),
                "monto", ticket.getPrecio(),
                "metodoPago", metodoPago != null ? metodoPago : "TARJETA"
        );

        try {
            Map resultado = webClientBuilder.build()
                    .post()
                    .uri(paymentMockUrl + "/api/v1/pagos/procesar")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .bodyValue(pagoBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("Pago procesado para ticket {}: {}", id, resultado);
        } catch (Exception e) {
            // Si el pago fue rechazado, propagar el error
            throw new RuntimeException("Pago rechazado: " + e.getMessage());
        }

        // Actualizar estado del ticket
        ticket.setEstado("PAGADO");
        Ticket guardado = repo.save(ticket);

        // Notificar al usuario (no crítico)
        if (ticket.getUsername() != null) {
            enviarNotificacion(
                    ticket.getUsername(),
                    "✅ Tu ticket #" + ticket.getId() + " para el evento " + ticket.getEventoId() + " ha sido PAGADO exitosamente. Precio: $" + ticket.getPrecio(),
                    bearerToken
            );
        }

        return guardado;
    }

    // ── WebClient Helpers ─────────────────────────────────────────────────────

    private void validarEvento(Long eventoId, String bearerToken) {
        if (eventoId == null) return;
        try {
            webClientBuilder.build()
                    .get()
                    .uri(eventCoreUrl + "/api/v1/eventos/" + eventoId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            log.info("Evento {} validado exitosamente en ms-event-core", eventoId);
        } catch (Exception e) {
            throw new RuntimeException("El evento con id " + eventoId + " no existe o no está disponible: " + e.getMessage());
        }
    }

    private void enviarNotificacion(String destinatario, String mensaje, String bearerToken) {
        try {
            Map<String, String> body = Map.of("destinatario", destinatario, "mensaje", mensaje);
            webClientBuilder.build()
                    .post()
                    .uri(notificationLogUrl + "/api/v1/notificaciones/enviar")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .subscribe(
                            r -> log.info("Notificación de pago enviada a {}", destinatario),
                            err -> log.warn("No se pudo enviar notificación: {}", err.getMessage())
                    );
        } catch (Exception e) {
            log.warn("Error al enviar notificación: {}", e.getMessage());
        }
    }
}
