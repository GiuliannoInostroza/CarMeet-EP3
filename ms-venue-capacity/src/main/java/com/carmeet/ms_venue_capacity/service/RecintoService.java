package com.carmeet.ms_venue_capacity.service;

import com.carmeet.ms_venue_capacity.model.Recinto;
import com.carmeet.ms_venue_capacity.repository.RecintoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecintoService {

    private final RecintoRepository repo;

    public Recinto registrarIngreso(Long id) {
        Recinto r = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recinto no encontrado"));

        if (r.getOcupacionActual() >= r.getCapacidadMaxima()) {
            throw new RuntimeException("¡Recinto Lleno! Capacidad máxima alcanzada.");
        }

        r.setOcupacionActual(r.getOcupacionActual() + 1);
        return repo.save(r);
    }
}
