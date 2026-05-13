package com.carmeet.ms_event_core.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @Builder
public class EventoDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank
    private String fecha;
    @NotBlank
    private String ubicacion;
}
