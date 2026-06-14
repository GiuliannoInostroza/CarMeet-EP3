package com.carmeet.ms_competition_reg.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

@Data
public class InscripcionDTO {
    private Long id;

    private Long vehiculoId;  // Opcional en creación

    @NotNull(message = "El eventoId es obligatorio")
    private Long eventoId;   // ← NUEVO

    @NotBlank(message = "La categoria es obligatoria")
    private String categoria;

    private String participante;  // Nombre del participante

    private String username;  // Opcional en creación

    private String estado;   // ← NUEVO: PENDIENTE | APROBADA | RECHAZADA

    @Valid
    private List<RequisitoDTO> requisitos;
}
