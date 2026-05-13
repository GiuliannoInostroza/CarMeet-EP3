package com.carmeet.ms_analytics_report.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;

@Data
public class ReporteDTO {
    private Long id;

    @NotNull(message = "El total de eventos es obligatorio")
    private Integer totalEventos;

    @NotBlank(message = "La fecha de generacion es obligatoria")
    private String fechaGeneracion;

    @Valid
    private List<MetricaDTO> metricas;
}
