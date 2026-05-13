package com.carmeet.ms_competition_reg.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

@Data
public class InscripcionDTO {
    private Long id;

    @NotNull(message = "El vehiculoId es obligatorio")
    private Long vehiculoId;

    @NotBlank(message = "La categoria es obligatoria")
    private String categoria;

    @NotBlank(message = "El username es obligatorio")
    private String username;

    @Valid
    private List<RequisitoDTO> requisitos;
}
