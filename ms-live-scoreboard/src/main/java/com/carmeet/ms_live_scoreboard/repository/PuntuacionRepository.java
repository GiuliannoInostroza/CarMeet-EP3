package com.carmeet.ms_live_scoreboard.repository;

import com.carmeet.ms_live_scoreboard.model.Puntuacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PuntuacionRepository extends JpaRepository<Puntuacion, Long> {
    List<Puntuacion> findByEventoIdOrderByPuntosDesc(Long eventoId);
    List<Puntuacion> findByInscripcionId(Long inscripcionId);
    List<Puntuacion> findTop10ByOrderByPuntosDesc();
}
