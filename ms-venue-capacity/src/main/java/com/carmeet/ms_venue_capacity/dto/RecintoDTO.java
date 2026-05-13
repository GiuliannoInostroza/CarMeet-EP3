package com.carmeet.ms_venue_capacity.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

@Data
public class RecintoDTO {
    private Long id;

    @NotNull(message = "La capacidad maxima es obligatoria")
    private Integer capacidadMaxima;

    @NotNull(message = "La ocupacion actual es obligatoria")
    private Integer ocupacionActual;

    @Valid
    private List<ZonaDTO> zonas;
}
