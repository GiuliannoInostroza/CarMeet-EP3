package com.carmeet.ms_analytics_report.service;

import com.carmeet.ms_analytics_report.model.Reporte;
import com.carmeet.ms_analytics_report.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ReporteRepository repo;
    private final WebClient webClient;

    public Reporte generarReporte(String token) {
        List<?> eventos = webClient.get()
                .uri("http://localhost:8091/api/events")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(List.class)
                .block();

        Reporte r = Reporte.builder()
                .totalEventos(eventos != null ? eventos.size() : 0)
                .fechaGeneracion(new java.util.Date().toString())
                .build();

        return repo.save(r);
    }
}
