package com.carmeet.ms_live_scoreboard.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PuntuacionDTO {
    @NotNull private Long inscripcionId;
    @NotNull private Integer puntos;
}
