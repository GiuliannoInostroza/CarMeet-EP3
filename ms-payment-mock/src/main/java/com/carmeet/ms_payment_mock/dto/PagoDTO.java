package com.carmeet.ms_payment_mock.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

@Data
public class PagoDTO {
    private Long id;

    @NotNull(message = "El ticketId es obligatorio")
    private Long ticketId;

    @NotNull(message = "El monto es obligatorio")
    private Double monto;

    @Valid
    private List<TransaccionLogDTO> logs;
}
