package com.carmeet.ms_ticketing.repository;

import com.carmeet.ms_ticketing.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByEventoId(Long eventoId);
    List<Ticket> findByUsername(String username);
}
