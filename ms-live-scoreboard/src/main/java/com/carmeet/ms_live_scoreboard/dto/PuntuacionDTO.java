package com.carmeet.ms_live_scoreboard.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

@Data
public class PuntuacionDTO {
    private Long id;

    @NotNull(message = "El inscripcionId es obligatorio")
    private Long inscripcionId;

    @NotNull(message = "Los puntos son obligatorios")
    private Integer puntos;

    private Long eventoId;  // ← NUEVO: referencia al evento (opcional)

    @Valid
    private List<DetallePuntuacionDTO> detalles;
}
