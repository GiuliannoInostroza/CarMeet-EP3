package com.carmeet.ms_venue_capacity.service;

import com.carmeet.ms_venue_capacity.model.Recinto;
import com.carmeet.ms_venue_capacity.repository.RecintoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecintoService {

    private final RecintoRepository repo;

    public List<Recinto> listar() {
        return repo.findAll();
    }

    public Recinto obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Recinto no encontrado con id: " + id));
    }

    public Recinto guardar(Recinto recinto) {
        if (recinto.getZonas() != null) {
            recinto.getZonas().forEach(z -> z.setRecinto(recinto));
        }
        return repo.save(recinto);
    }

    public Recinto actualizar(Long id, Recinto datosNuevos) {
        Recinto existente = obtenerPorId(id);
        existente.setCapacidadMaxima(datosNuevos.getCapacidadMaxima());
        existente.setOcupacionActual(datosNuevos.getOcupacionActual());
        
        existente.getZonas().clear();
        if (datosNuevos.getZonas() != null) {
            datosNuevos.getZonas().forEach(z -> {
                z.setRecinto(existente);
                existente.getZonas().add(z);
            });
        }
        
        return repo.save(existente);
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Recinto no encontrado con id: " + id);
        }
        repo.deleteById(id);
    }

    public Recinto registrarIngreso(Long id) {
        Recinto r = obtenerPorId(id);
        if (r.getOcupacionActual() >= r.getCapacidadMaxima()) {
            throw new RuntimeException("¡Recinto Lleno! Capacidad máxima alcanzada.");
        }
        r.setOcupacionActual(r.getOcupacionActual() + 1);
        return repo.save(r);
    }
}
