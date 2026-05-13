package com.carmeet.ms_notification_log.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;

@Data
public class NotificacionDTO {
    private Long id;

    @NotBlank(message = "El destinatario es obligatorio")
    private String destinatario;

    @NotBlank(message = "El mensaje es obligatorio")
    private String mensaje;

    @Valid
    private List<AdjuntoDTO> adjuntos;
}
