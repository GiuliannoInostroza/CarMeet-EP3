package com.carmeet.ms_analytics_report.service;

import com.carmeet.ms_analytics_report.client.CompetitionRegClient;
import com.carmeet.ms_analytics_report.client.EventoCoreClient;
import com.carmeet.ms_analytics_report.client.TicketingClient;
import com.carmeet.ms_analytics_report.dto.MetricaDTO;
import com.carmeet.ms_analytics_report.model.Metrica;
import com.carmeet.ms_analytics_report.model.Reporte;
import com.carmeet.ms_analytics_report.repository.ReporteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ReporteRepository repo;
    private final EventoCoreClient eventoClient;
    private final TicketingClient ticketingClient;
    private final CompetitionRegClient competitionClient;

    public List<Reporte> listar() {
        return repo.findAll();
    }

    public Reporte obtenerPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado con id: " + id));
    }

    public Reporte guardar(Reporte reporte) {
        if (reporte.getMetricas() != null) {
            reporte.getMetricas().forEach(m -> m.setReporte(reporte));
        }
        return repo.save(reporte);
    }

    public Reporte actualizar(Long id, Reporte datosNuevos) {
        Reporte existente = obtenerPorId(id);
        existente.setEventoId(datosNuevos.getEventoId());
        existente.setTotalEventos(datosNuevos.getTotalEventos());
        existente.setTotalTickets(datosNuevos.getTotalTickets());
        existente.setTotalInscripciones(datosNuevos.getTotalInscripciones());
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

    public Reporte generarReporte(Long eventoId, String bearerToken) {
        eventoClient.validarEvento(eventoId, bearerToken);

        int totalTickets = 0;
        try {
            totalTickets = ticketingClient.contarTicketsPorEvento(eventoId, bearerToken);
        } catch (Exception e) {
            // si ticketing no responde, el reporte se genera igual con 0
        }

        int totalInscripciones = 0;
        try {
            totalInscripciones = competitionClient.contarInscripcionesPorEvento(eventoId, bearerToken);
        } catch (Exception e) {
            // si competition no responde, el reporte se genera igual con 0
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Reporte reporte = new Reporte();
        reporte.setEventoId(eventoId);
        reporte.setTotalEventos(1);
        reporte.setTotalTickets(totalTickets);
        reporte.setTotalInscripciones(totalInscripciones);
        reporte.setFechaGeneracion(now);

        List<Metrica> metricas = new ArrayList<>();

        Metrica mTickets = new Metrica();
        mTickets.setNombre("total_tickets");
        mTickets.setValor((double) totalTickets);
        mTickets.setReporte(reporte);
        metricas.add(mTickets);

        Metrica mInscripciones = new Metrica();
        mInscripciones.setNombre("total_inscripciones");
        mInscripciones.setValor((double) totalInscripciones);
        mInscripciones.setReporte(reporte);
        metricas.add(mInscripciones);

        reporte.setMetricas(metricas);
        return repo.save(reporte);
    }

    public List<MetricaDTO> resumenMetricas() {
        Map<String, Double> agrupado = repo.findAll().stream()
                .filter(r -> r.getMetricas() != null)
                .flatMap(r -> r.getMetricas().stream())
                .collect(Collectors.groupingBy(
                        Metrica::getNombre,
                        Collectors.summingDouble(m -> m.getValor() != null ? m.getValor() : 0.0)
                ));

        return agrupado.entrySet().stream()
                .map(e -> {
                    MetricaDTO dto = new MetricaDTO();
                    dto.setNombre(e.getKey());
                    dto.setValor(e.getValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
