package com.carmeet.ms_venue_capacity.service;

import com.carmeet.ms_venue_capacity.model.Recinto;
import com.carmeet.ms_venue_capacity.model.Zona;
import com.carmeet.ms_venue_capacity.repository.RecintoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

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
        existente.setNombre(datosNuevos.getNombre());
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

    /** Lista las zonas de un recinto */
    public List<Zona> listarZonas(Long recintoId) {
        return obtenerPorId(recintoId).getZonas();
    }

    /**
     * Retorna información de disponibilidad del recinto:
     * { disponible, capacidadMaxima, ocupacionActual, plazasLibres }
     */
    public Map<String, Object> consultarDisponibilidad(Long id) {
        Recinto r = obtenerPorId(id);
        int plazasLibres = r.getCapacidadMaxima() - r.getOcupacionActual();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("recintoId", r.getId());
        info.put("nombre", r.getNombre());
        info.put("disponible", plazasLibres > 0);
        info.put("capacidadMaxima", r.getCapacidadMaxima());
        info.put("ocupacionActual", r.getOcupacionActual());
        info.put("plazasLibres", plazasLibres);
        return info;
    }

    /** Registra el ingreso de una persona al recinto */
    public Recinto registrarIngreso(Long id) {
        Recinto r = obtenerPorId(id);
        if (r.getOcupacionActual() >= r.getCapacidadMaxima()) {
            throw new RuntimeException("¡Recinto lleno! Capacidad máxima alcanzada: " + r.getCapacidadMaxima());
        }
        r.setOcupacionActual(r.getOcupacionActual() + 1);
        return repo.save(r);
    }

    /** Registra el egreso de una persona del recinto */
    public Recinto registrarEgreso(Long id) {
        Recinto r = obtenerPorId(id);
        if (r.getOcupacionActual() <= 0) {
            throw new RuntimeException("La ocupación del recinto ya es 0, no se puede registrar egreso.");
        }
        r.setOcupacionActual(r.getOcupacionActual() - 1);
        return repo.save(r);
    }
}
