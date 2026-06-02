package com.carmeet.ms_competition_reg.service;

import com.carmeet.ms_competition_reg.client.EventoCoreClient;
import com.carmeet.ms_competition_reg.client.NotificacionLogClient;
import com.carmeet.ms_competition_reg.client.VehiculoRegistryClient;
import com.carmeet.ms_competition_reg.model.Inscripcion;
import com.carmeet.ms_competition_reg.repository.InscripcionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InscripcionService {

    private final InscripcionRepository repo;
    private final EventoCoreClient eventoClient;
    private final VehiculoRegistryClient vehiculoClient;
    private final NotificacionLogClient notificacionClient;

    public List<Inscripcion> listar() {
        return repo.findAll();
    }

    public Inscripcion obtenerPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inscripcion no encontrada con id: " + id));
    }

    public Inscripcion guardar(Inscripcion inscripcion, String bearerToken) {
        eventoClient.validarEvento(inscripcion.getEventoId(), bearerToken);
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

    public List<Inscripcion> obtenerPorEventoId(Long eventoId) {
        return repo.findByEventoId(eventoId);
    }

    public List<Inscripcion> obtenerPorVehiculoId(Long vehiculoId) {
        return repo.findByVehiculoId(vehiculoId);
    }

    public Inscripcion aprobar(Long id, String bearerToken) {
        Inscripcion inscripcion = obtenerPorId(id);
        if (!"PENDIENTE".equals(inscripcion.getEstado())) {
            throw new RuntimeException("Solo se pueden aprobar inscripciones en estado PENDIENTE. Estado actual: " + inscripcion.getEstado());
        }
        if (inscripcion.getVehiculoId() != null) {
            vehiculoClient.validarVehiculo(inscripcion.getVehiculoId(), bearerToken);
        }
        inscripcion.setEstado("APROBADA");
        Inscripcion guardada = repo.save(inscripcion);
        if (inscripcion.getUsername() != null) {
            notificacionClient.enviar(
                    inscripcion.getUsername(),
                    "Tu inscripcion #" + inscripcion.getId() + " ha sido APROBADA. Categoria: " + inscripcion.getCategoria(),
                    bearerToken
            );
        }
        return guardada;
    }

    public Inscripcion rechazar(Long id, String bearerToken) {
        Inscripcion inscripcion = obtenerPorId(id);
        if (!"PENDIENTE".equals(inscripcion.getEstado())) {
            throw new RuntimeException("Solo se pueden rechazar inscripciones en estado PENDIENTE. Estado actual: " + inscripcion.getEstado());
        }
        inscripcion.setEstado("RECHAZADA");
        Inscripcion guardada = repo.save(inscripcion);
        if (inscripcion.getUsername() != null) {
            notificacionClient.enviar(
                    inscripcion.getUsername(),
                    "Tu inscripcion #" + inscripcion.getId() + " ha sido RECHAZADA.",
                    bearerToken
            );
        }
        return guardada;
    }
}
