package com.carmeet.ms_venue_capacity.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;

@Data
public class RecintoDTO {
    private Long id;

    @NotBlank(message = "El nombre del recinto es obligatorio")
    private String nombre;

    private Integer capacidad;        // alias amigable para el JSON
    private Integer capacidadMaxima;  // campo real del modelo
    private Integer ocupacionActual;  // opcional, default 0 al crear

    @Valid
    private List<ZonaDTO> zonas;
}

