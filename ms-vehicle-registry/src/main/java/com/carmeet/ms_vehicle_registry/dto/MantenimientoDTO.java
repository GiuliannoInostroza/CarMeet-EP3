package com.carmeet.ms_vehicle_registry.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MantenimientoDTO {
    
    @NotBlank(message = "La descripción del mantenimiento es obligatoria")
    private String descripcion;
}
