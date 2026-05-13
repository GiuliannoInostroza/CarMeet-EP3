package com.carmeet.ms_analytics_report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricaDTO {
    
    @NotBlank(message = "El nombre de la métrica es obligatorio")
    private String nombre;

    @NotNull(message = "El valor es obligatorio")
    private Double valor;
}
