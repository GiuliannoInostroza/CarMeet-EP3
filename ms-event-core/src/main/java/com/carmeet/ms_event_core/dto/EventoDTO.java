package com.carmeet.ms_event_core.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

@Data
public class EventoDTO {
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La fecha es obligatoria")
    private String fecha;

    @NotBlank(message = "La ubicacion es obligatoria")
    private String ubicacion;

    @Valid
    private List<PatrocinadorDTO> patrocinadores;
}
