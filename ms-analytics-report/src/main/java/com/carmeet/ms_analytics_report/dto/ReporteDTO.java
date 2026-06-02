package com.carmeet.ms_analytics_report.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.Valid;

@Data
public class ReporteDTO {
    private Long id;
    private Long eventoId;
    private Integer totalEventos;
    private Integer totalTickets;
    private Integer totalInscripciones;
    private String fechaGeneracion;

    @Valid
    private List<MetricaDTO> metricas;
}
