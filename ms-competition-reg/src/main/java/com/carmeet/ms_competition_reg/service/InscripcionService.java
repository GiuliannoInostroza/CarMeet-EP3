package com.carmeet.ms_competition_reg.service;

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

    public List<Inscripcion> listar() {
        return repo.findAll();
    }

    public Inscripcion obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Inscripcion no encontrada con id: " + id));
    }

    public Inscripcion guardar(Inscripcion inscripcion) {
        if (inscripcion.getRequisitos() != null) {
            inscripcion.getRequisitos().forEach(r -> r.setInscripcion(inscripcion));
        }
        return repo.save(inscripcion);
    }

    public Inscripcion actualizar(Long id, Inscripcion datosNuevos) {
        Inscripcion existente = obtenerPorId(id);
        existente.setVehiculoId(datosNuevos.getVehiculoId());
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
}
