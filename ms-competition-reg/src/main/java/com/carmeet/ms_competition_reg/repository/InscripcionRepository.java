package com.carmeet.ms_competition_reg.repository;

import com.carmeet.ms_competition_reg.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    List<Inscripcion> findByEventoId(Long eventoId);
    List<Inscripcion> findByVehiculoId(Long vehiculoId);
    List<Inscripcion> findByUsername(String username);
}
