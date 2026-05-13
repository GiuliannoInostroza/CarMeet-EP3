package com.carmeet.ms_payment_mock.service;

import com.carmeet.ms_payment_mock.dto.PagoDTO;
import com.carmeet.ms_payment_mock.model.Pago;
import com.carmeet.ms_payment_mock.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository repo;

    public void procesarPago(PagoDTO dto) {
        log.info("Procesando pago de ticket {} por monto {}", dto.getTicketId(), dto.getMonto());
        
        Pago p = Pago.builder()
                .ticketId(dto.getTicketId())
                .monto(dto.getMonto())
                .build();
                
        repo.save(p);
        // Simulación: siempre exitoso
    }
}
