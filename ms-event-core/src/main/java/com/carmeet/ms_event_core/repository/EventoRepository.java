package com.carmeet.ms_event_core.repository;

import com.carmeet.ms_event_core.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    /** Eventos cuya fecha es mayor o igual a la fecha provista (formato 'yyyy-MM-dd') */
    @Query("SELECT e FROM Evento e WHERE e.fecha >= :hoy ORDER BY e.fecha ASC")
    List<Evento> findEventosProximos(@Param("hoy") String hoy);

    /** Búsqueda de eventos por nombre (case-insensitive, parcial) */
    List<Evento> findByNombreContainingIgnoreCase(String nombre);
}
