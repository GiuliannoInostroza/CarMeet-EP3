package com.carmeet.ms_payment_mock.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

@Data
public class PagoDTO {
    private Long id;

    private Long ticketId;  // Opcional en modo mock

    @NotNull(message = "El monto es obligatorio")
    private Double monto;

    private String metodoPago;

    @Valid
    private List<TransaccionLogDTO> logs;
}

