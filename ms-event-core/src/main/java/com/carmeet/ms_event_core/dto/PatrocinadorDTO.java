package com.carmeet.ms_event_core.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrocinadorDTO {
    
    @NotBlank(message = "El nombre del patrocinador es obligatorio")
    private String nombre;
    
    @NotBlank(message = "El nivel es obligatorio")
    private String nivel;
}
