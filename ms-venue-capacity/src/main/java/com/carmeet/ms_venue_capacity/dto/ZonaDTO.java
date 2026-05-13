package com.carmeet.ms_venue_capacity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZonaDTO {

    private Long id;

    @NotBlank(message = "El nombre de la zona es obligatorio")
    private String nombre;
}
