package com.carmeet.ms_vehicle_registry.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VehiculoDTO {
    @NotBlank private String patente;
    @NotBlank private String marca;
    @NotBlank private String modelo;
}
