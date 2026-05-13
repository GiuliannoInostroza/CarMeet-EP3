package com.carmeet.ms_ticketing.repository;

import com.carmeet.ms_ticketing.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {}
