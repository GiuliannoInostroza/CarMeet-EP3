package com.carmeet.ms_competition_reg.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;

@Data
public class InscripcionDTO {
    private Long id;

    private Long vehiculoId;  // Opcional en creación

    @NotBlank(message = "La categoria es obligatoria")
    private String categoria;

    private String participante;  // Nombre del participante

    private String username;  // Opcional en creación

    @Valid
    private List<RequisitoDTO> requisitos;
}

