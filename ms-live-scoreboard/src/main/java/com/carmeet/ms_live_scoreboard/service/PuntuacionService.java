package com.carmeet.ms_live_scoreboard.service;

import com.carmeet.ms_live_scoreboard.client.CompetitionRegClient;
import com.carmeet.ms_live_scoreboard.model.Puntuacion;
import com.carmeet.ms_live_scoreboard.repository.PuntuacionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PuntuacionService {

    private final PuntuacionRepository repo;
    private final CompetitionRegClient competitionClient;

    public List<Puntuacion> listar() {
        return repo.findAll();
    }

    public Puntuacion obtenerPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Puntuacion no encontrada con id: " + id));
    }

    public Puntuacion guardar(Puntuacion puntuacion, String bearerToken) {
        competitionClient.validarInscripcion(puntuacion.getInscripcionId(), bearerToken);
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

    public List<Puntuacion> rankingPorEvento(Long eventoId) {
        return repo.findByEventoIdOrderByPuntosDesc(eventoId);
    }

    public List<Puntuacion> porInscripcion(Long inscripcionId) {
        return repo.findByInscripcionId(inscripcionId);
    }

    public List<Puntuacion> top10Global() {
        return repo.findTop10ByOrderByPuntosDesc();
    }
}
