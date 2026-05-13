package com.carmeet.ms_competition_reg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequisitoDTO {
    
    @NotBlank(message = "El nombre del requisito es obligatorio")
    private String nombre;
}
