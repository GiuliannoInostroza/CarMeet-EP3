package com.carmeet.ms_competition_reg.service;

import com.carmeet.ms_competition_reg.model.Inscripcion;
import com.carmeet.ms_competition_reg.model.Requisito;
import com.carmeet.ms_competition_reg.repository.InscripcionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class InscripcionService {

    private final InscripcionRepository repo;
    private final WebClient.Builder webClientBuilder;

    @Value("${services.event-core.base-url}")
    private String eventCoreUrl;

    @Value("${services.vehicle-registry.base-url}")
    private String vehicleRegistryUrl;

    @Value("${services.notification-log.base-url}")
    private String notificationLogUrl;

    public InscripcionService(InscripcionRepository repo, WebClient.Builder webClientBuilder) {
        this.repo = repo;
        this.webClientBuilder = webClientBuilder;
    }

    public List<Inscripcion> listar() {
        return repo.findAll();
    }

    public Inscripcion obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Inscripcion no encontrada con id: " + id));
    }

    /**
     * Crea una inscripción. Valida que el evento exista via WebClient.
     * El token JWT se propaga para autenticación inter-servicio.
     */
    public Inscripcion guardar(Inscripcion inscripcion, String bearerToken) {
        // Validar que el evento existe en ms-event-core
        validarEvento(inscripcion.getEventoId(), bearerToken);

        if (inscripcion.getRequisitos() != null) {
            inscripcion.getRequisitos().forEach(r -> r.setInscripcion(inscripcion));
        }
        inscripcion.setEstado("PENDIENTE");
        return repo.save(inscripcion);
    }

    public Inscripcion actualizar(Long id, Inscripcion datosNuevos) {
        Inscripcion existente = obtenerPorId(id);
        existente.setVehiculoId(datosNuevos.getVehiculoId());
        existente.setEventoId(datosNuevos.getEventoId());
        existente.setParticipante(datosNuevos.getParticipante());
        existente.setCategoria(datosNuevos.getCategoria());
        existente.setUsername(datosNuevos.getUsername());

        existente.getRequisitos().clear();
        if (datosNuevos.getRequisitos() != null) {
            datosNuevos.getRequisitos().forEach(r -> {
                r.setInscripcion(existente);
                existente.getRequisitos().add(r);
            });
        }

        return repo.save(existente);
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Inscripcion no encontrada con id: " + id);
        }
        repo.deleteById(id);
    }

    /** Inscripciones de un evento */
    public List<Inscripcion> obtenerPorEventoId(Long eventoId) {
        return repo.findByEventoId(eventoId);
    }

    /** Inscripciones de un vehículo */
    public List<Inscripcion> obtenerPorVehiculoId(Long vehiculoId) {
        return repo.findByVehiculoId(vehiculoId);
    }

    /**
     * Aprueba una inscripción:
     * 1. Valida que el vehículo existe en ms-vehicle-registry.
     * 2. Cambia estado a APROBADA.
     * 3. Notifica al participante via ms-notification-log.
     */
    public Inscripcion aprobar(Long id, String bearerToken) {
        Inscripcion inscripcion = obtenerPorId(id);
        if (!"PENDIENTE".equals(inscripcion.getEstado())) {
            throw new RuntimeException("Solo se pueden aprobar inscripciones en estado PENDIENTE. Estado actual: " + inscripcion.getEstado());
        }

        // Validar vehículo en ms-vehicle-registry
        if (inscripcion.getVehiculoId() != null) {
            validarVehiculo(inscripcion.getVehiculoId(), bearerToken);
        }

        inscripcion.setEstado("APROBADA");
        Inscripcion guardada = repo.save(inscripcion);

        // Notificar al participante
        if (inscripcion.getUsername() != null) {
            enviarNotificacion(
                    inscripcion.getUsername(),
                    "¡Tu inscripción #" + inscripcion.getId() + " ha sido APROBADA! Categoría: " + inscripcion.getCategoria(),
                    bearerToken
            );
        }

        return guardada;
    }

    /**
     * Rechaza una inscripción:
     * 1. Cambia estado a RECHAZADA.
     * 2. Notifica al participante.
     */
    public Inscripcion rechazar(Long id, String bearerToken) {
        Inscripcion inscripcion = obtenerPorId(id);
        if (!"PENDIENTE".equals(inscripcion.getEstado())) {
            throw new RuntimeException("Solo se pueden rechazar inscripciones en estado PENDIENTE. Estado actual: " + inscripcion.getEstado());
        }

        inscripcion.setEstado("RECHAZADA");
        Inscripcion guardada = repo.save(inscripcion);

        // Notificar al participante
        if (inscripcion.getUsername() != null) {
            enviarNotificacion(
                    inscripcion.getUsername(),
                    "Tu inscripción #" + inscripcion.getId() + " ha sido RECHAZADA. Por favor, contacta al organizador.",
                    bearerToken
            );
        }

        return guardada;
    }

    // ── WebClient Helpers ─────────────────────────────────────────────────────

    /** Valida que el evento exista en ms-event-core */
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

    /** Valida que el vehículo exista en ms-vehicle-registry */
    private void validarVehiculo(Long vehiculoId, String bearerToken) {
        try {
            webClientBuilder.build()
                    .get()
                    .uri(vehicleRegistryUrl + "/api/v1/vehiculos/" + vehiculoId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            log.info("Vehículo {} validado exitosamente en ms-vehicle-registry", vehiculoId);
        } catch (Exception e) {
            throw new RuntimeException("El vehículo con id " + vehiculoId + " no existe o no está disponible: " + e.getMessage());
        }
    }

    /** Envía notificación vía ms-notification-log */
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
                            r -> log.info("Notificación enviada a {}", destinatario),
                            err -> log.warn("No se pudo enviar notificación a {}: {}", destinatario, err.getMessage())
                    );
        } catch (Exception e) {
            // La notificación es no crítica — no detiene el flujo principal
            log.warn("Error al enviar notificación: {}", e.getMessage());
        }
    }
}
