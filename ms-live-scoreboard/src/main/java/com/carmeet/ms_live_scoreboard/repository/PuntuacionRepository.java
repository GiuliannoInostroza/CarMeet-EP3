package com.carmeet.ms_live_scoreboard.repository;

import com.carmeet.ms_live_scoreboard.model.Puntuacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PuntuacionRepository extends JpaRepository<Puntuacion, Long> {
}
