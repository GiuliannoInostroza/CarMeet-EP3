package com.carmeet.ms_event_core.repository;

import com.carmeet.ms_event_core.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, Long> {}
