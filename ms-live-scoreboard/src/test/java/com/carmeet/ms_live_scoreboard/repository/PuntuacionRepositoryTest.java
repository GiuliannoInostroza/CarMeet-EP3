package com.carmeet.ms_live_scoreboard.repository;

import com.carmeet.ms_live_scoreboard.model.Puntuacion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PuntuacionRepositoryTest {

    @Autowired
    private PuntuacionRepository repository;

    @Test
    void debeGuardarPuntuacion() {
        Puntuacion p = Puntuacion.builder()
                .inscripcionId(1L)
                .eventoId(2L)
                .puntos(85)
                .build();

        Puntuacion guardada = repository.save(p);
        assertNotNull(guardada.getId());
        assertEquals(85, guardada.getPuntos());
    }

    @Test
    void debeBuscarPuntuacionPorId() {
        Puntuacion p = Puntuacion.builder()
                .inscripcionId(3L)
                .eventoId(4L)
                .puntos(90)
                .build();

        Puntuacion guardada = repository.save(p);
        Optional<Puntuacion> resultado = repository.findById(guardada.getId());
        assertTrue(resultado.isPresent());
        assertEquals(90, resultado.get().getPuntos());
    }

    @Test
    void debeListarPuntuaciones() {
        repository.save(Puntuacion.builder().inscripcionId(1L).eventoId(1L).puntos(50).build());
        repository.save(Puntuacion.builder().inscripcionId(2L).eventoId(1L).puntos(60).build());

        List<Puntuacion> resultado = repository.findAll();
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.size() >= 2);
    }

    @Test
    void debeEliminarPuntuacion() {
        Puntuacion p = Puntuacion.builder().inscripcionId(3L).eventoId(3L).puntos(70).build();
        Puntuacion guardada = repository.save(p);

        repository.deleteById(guardada.getId());
        Optional<Puntuacion> resultado = repository.findById(guardada.getId());
        assertFalse(resultado.isPresent());
    }

    @Test
    void debeBuscarPorEventoIdOrdenadoPorPuntosDesc() {
        repository.save(Puntuacion.builder().inscripcionId(1L).eventoId(10L).puntos(75).build());
        repository.save(Puntuacion.builder().inscripcionId(2L).eventoId(10L).puntos(95).build());
        repository.save(Puntuacion.builder().inscripcionId(3L).eventoId(10L).puntos(85).build());

        List<Puntuacion> ranking = repository.findByEventoIdOrderByPuntosDesc(10L);
        assertEquals(3, ranking.size());
        assertEquals(95, ranking.get(0).getPuntos());
        assertEquals(85, ranking.get(1).getPuntos());
        assertEquals(75, ranking.get(2).getPuntos());
    }

    @Test
    void debeBuscarPorInscripcionId() {
        repository.save(Puntuacion.builder().inscripcionId(100L).eventoId(1L).puntos(80).build());
        repository.save(Puntuacion.builder().inscripcionId(200L).eventoId(1L).puntos(90).build());

        List<Puntuacion> resultados = repository.findByInscripcionId(100L);
        assertEquals(1, resultados.size());
        assertEquals(80, resultados.get(0).getPuntos());
    }

    @Test
    void debeBuscarTop10GlobalOrdenadoPorPuntosDesc() {
        for (int i = 1; i <= 12; i++) {
            repository.save(Puntuacion.builder().inscripcionId((long) i).eventoId(1L).puntos(i * 5).build());
        }

        List<Puntuacion> top10 = repository.findTop10ByOrderByPuntosDesc();
        assertEquals(10, top10.size());
        assertEquals(60, top10.get(0).getPuntos()); // 12 * 5
        assertEquals(55, top10.get(1).getPuntos()); // 11 * 5
    }
}
