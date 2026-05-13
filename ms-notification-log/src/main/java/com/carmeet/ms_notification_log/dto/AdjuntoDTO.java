package com.carmeet.ms_notification_log.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjuntoDTO {
    
    @NotBlank(message = "El nombre del archivo es obligatorio")
    private String nombreArchivo;

    @NotBlank(message = "La URL del adjunto es obligatoria")
    private String url;
}
