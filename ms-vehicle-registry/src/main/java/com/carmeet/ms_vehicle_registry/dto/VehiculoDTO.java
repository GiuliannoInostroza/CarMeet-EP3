package com.carmeet.ms_vehicle_registry.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

@Data
public class VehiculoDTO {
    private Long id;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;

    @NotNull(message = "El anio es obligatorio")
    private Integer anio;

    @Valid
    private List<MantenimientoDTO> mantenimientos;
}
