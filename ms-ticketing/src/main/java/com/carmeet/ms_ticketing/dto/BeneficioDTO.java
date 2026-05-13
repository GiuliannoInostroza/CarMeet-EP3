package com.carmeet.ms_ticketing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficioDTO {

    private Long id;

    @NotBlank(message = "El nombre del beneficio es obligatorio")
    private String nombre;

    private String descripcion;
}
