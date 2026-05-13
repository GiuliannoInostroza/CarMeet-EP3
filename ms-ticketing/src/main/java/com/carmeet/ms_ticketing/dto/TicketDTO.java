package com.carmeet.ms_ticketing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketDTO {
    @NotNull private Long eventoId;
    @NotNull private Double precio;
}
