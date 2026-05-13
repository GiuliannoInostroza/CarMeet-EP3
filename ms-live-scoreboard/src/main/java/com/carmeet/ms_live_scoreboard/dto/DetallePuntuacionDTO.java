package com.carmeet.ms_live_scoreboard.dto;

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
public class DetallePuntuacionDTO {

    private Long id;

    @NotBlank(message = "La categoria del detalle es obligatoria")
    private String categoria;

    @NotNull
    private Integer puntosAsignados;

    private String descripcion;
}
