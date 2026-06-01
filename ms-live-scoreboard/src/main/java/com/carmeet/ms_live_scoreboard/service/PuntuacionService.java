package com.carmeet.ms_live_scoreboard.service;

import com.carmeet.ms_live_scoreboard.model.Puntuacion;
import com.carmeet.ms_live_scoreboard.repository.PuntuacionRepository;
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
public class PuntuacionService {

    private final PuntuacionRepository repo;
    private final WebClient.Builder webClientBuilder;

    @Value("${services.competition-reg.base-url}")
    private String competitionRegUrl;

    public PuntuacionService(PuntuacionRepository repo, WebClient.Builder webClientBuilder) {
        this.repo = repo;
        this.webClientBuilder = webClientBuilder;
    }

    public List<Puntuacion> listar() {
        return repo.findAll();
    }

    public Puntuacion obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Puntuacion no encontrada con id: " + id));
    }

    /**
     * Registra una puntuación. Valida que la inscripción exista via WebClient.
     */
    public Puntuacion guardar(Puntuacion puntuacion, String bearerToken) {
        // Validar que la inscripción existe en ms-competition-reg
        validarInscripcion(puntuacion.getInscripcionId(), bearerToken);

        if (puntuacion.getDetalles() != null) {
            puntuacion.getDetalles().forEach(d -> d.setPuntuacion(puntuacion));
        }
        return repo.save(puntuacion);
    }

    public Puntuacion actualizar(Long id, Puntuacion datosNuevos) {
        Puntuacion existente = obtenerPorId(id);
        existente.setInscripcionId(datosNuevos.getInscripcionId());
        existente.setEventoId(datosNuevos.getEventoId());
        existente.setPuntos(datosNuevos.getPuntos());

        existente.getDetalles().clear();
        if (datosNuevos.getDetalles() != null) {
            datosNuevos.getDetalles().forEach(d -> {
                d.setPuntuacion(existente);
                existente.getDetalles().add(d);
            });
        }

        return repo.save(existente);
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Puntuacion no encontrada con id: " + id);
        }
        repo.deleteById(id);
    }

    /** Ranking de un evento ordenado por puntos desc */
    public List<Puntuacion> rankingPorEvento(Long eventoId) {
        return repo.findByEventoIdOrderByPuntosDesc(eventoId);
    }

    /** Puntuaciones de una inscripción */
    public List<Puntuacion> porInscripcion(Long inscripcionId) {
        return repo.findByInscripcionId(inscripcionId);
    }

    /** Top 10 global */
    public List<Puntuacion> top10Global() {
        return repo.findTop10ByOrderByPuntosDesc();
    }

    // ── WebClient Helper ──────────────────────────────────────────────────────

    private void validarInscripcion(Long inscripcionId, String bearerToken) {
        if (inscripcionId == null) return;
        try {
            webClientBuilder.build()
                    .get()
                    .uri(competitionRegUrl + "/api/v1/inscripciones/" + inscripcionId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            log.info("Inscripción {} validada exitosamente en ms-competition-reg", inscripcionId);
        } catch (Exception e) {
            throw new RuntimeException("La inscripción con id " + inscripcionId + " no existe o no está disponible: " + e.getMessage());
        }
    }
}
