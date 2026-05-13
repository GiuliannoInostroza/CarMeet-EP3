package com.carmeet.ms_payment_mock.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionLogDTO {
    
    @NotBlank(message = "El estado del log es obligatorio")
    private String estado;
}
