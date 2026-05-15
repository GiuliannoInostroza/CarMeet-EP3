package com.carmeet.ms_ticketing.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

@Data
public class TicketDTO {
    private Long id;

    @NotNull(message = "El eventoId es obligatorio")
    private Long eventoId;

    @NotNull(message = "El precio es obligatorio")
    private Double precio;

    @NotBlank(message = "La categoria es obligatoria")
    private String categoria;

    private String estado;    // Se puede asignar un default en el servicio
    private String username;  // Opcional en creación

    @Valid
    private List<BeneficioDTO> beneficios;
}

