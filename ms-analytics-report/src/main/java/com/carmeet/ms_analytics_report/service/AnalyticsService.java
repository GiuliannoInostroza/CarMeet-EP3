package com.carmeet.ms_analytics_report.service;

import com.carmeet.ms_analytics_report.model.Reporte;
import com.carmeet.ms_analytics_report.repository.ReporteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ReporteRepository repo;

    public List<Reporte> listar() {
        return repo.findAll();
    }

    public Reporte obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado con id: " + id));
    }

    public Reporte guardar(Reporte reporte) {
        if (reporte.getMetricas() != null) {
            reporte.getMetricas().forEach(m -> m.setReporte(reporte));
        }
        return repo.save(reporte);
    }

    public Reporte actualizar(Long id, Reporte datosNuevos) {
        Reporte existente = obtenerPorId(id);
        existente.setTotalEventos(datosNuevos.getTotalEventos());
        existente.setFechaGeneracion(datosNuevos.getFechaGeneracion());
        
        existente.getMetricas().clear();
        if (datosNuevos.getMetricas() != null) {
            datosNuevos.getMetricas().forEach(m -> {
                m.setReporte(existente);
                existente.getMetricas().add(m);
            });
        }
        
        return repo.save(existente);
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Reporte no encontrado con id: " + id);
        }
        repo.deleteById(id);
    }
}
