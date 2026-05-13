package com.carmeet.ms_ticketing.service;

import com.carmeet.ms_ticketing.dto.TicketDTO;
import com.carmeet.ms_ticketing.model.Ticket;
import com.carmeet.ms_ticketing.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository repo;
    private final WebClient webClient;

    public Ticket comprar(TicketDTO dto, String token) {
        
        Ticket t = Ticket.builder()
                .eventoId(dto.getEventoId())
                .precio(dto.getPrecio())
                .estado("PENDIENTE")
                .username(SecurityContextHolder.getContext().getAuthentication().getName())
                .build();

        t = repo.save(t);

        try {
            webClient.post()
                    .uri("http://localhost:8094/api/payments/procesar")
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(t)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            
            t.setEstado("PAGADO");
        } catch (Exception e) {
            t.setEstado("RECHAZADO");
        }

        return repo.save(t);
    }
}
