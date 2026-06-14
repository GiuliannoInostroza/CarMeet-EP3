package com.carmeet.ms_event_core.repository;

import com.carmeet.ms_event_core.model.Evento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class EventoRepositoryTest {

    @Autowired
    private EventoRepository repository;

    @Test
    void debeGuardarEvento() {
        Evento e = Evento.builder()
                .nombre("CarMeet EP3")
                .fecha("2026-06-20")
                .ubicacion("Track A")
                .build();

        Evento guardado = repository.save(e);
        assertNotNull(guardado.getId());
        assertEquals("CarMeet EP3", guardado.getNombre());
    }

    @Test
    void debeBuscarEventoPorId() {
        Evento e = Evento.builder()
                .nombre("Drift Night")
                .fecha("2026-06-25")
                .ubicacion("Track B")
                .build();

        Evento guardado = repository.save(e);
        Optional<Evento> resultado = repository.findById(guardado.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Drift Night", resultado.get().getNombre());
    }

    @Test
    void debeListarEventos() {
        repository.save(Evento.builder().nombre("E1").fecha("2026-06-01").build());
        repository.save(Evento.builder().nombre("E2").fecha("2026-06-02").build());

        List<Evento> resultado = repository.findAll();
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.size() >= 2);
    }

    @Test
    void debeEliminarEvento() {
        Evento e = Evento.builder().nombre("E3").fecha("2026-06-03").build();
        Evento guardado = repository.save(e);

        repository.deleteById(guardado.getId());
        Optional<Evento> resultado = repository.findById(guardado.getId());
        assertFalse(resultado.isPresent());
    }

    @Test
    void debeBuscarEventosProximos() {
        repository.save(Evento.builder().nombre("Pasado").fecha("2026-06-10").build());
        repository.save(Evento.builder().nombre("Hoy").fecha("2026-06-14").build());
        repository.save(Evento.builder().nombre("Futuro").fecha("2026-06-18").build());

        List<Evento> proximos = repository.findEventosProximos("2026-06-14");
        assertEquals(2, proximos.size());
        assertEquals("Hoy", proximos.get(0).getNombre());
        assertEquals("Futuro", proximos.get(1).getNombre());
    }

    @Test
    void debeBuscarPorNombreConteniendoIgnorarMayusculas() {
        repository.save(Evento.builder().nombre("Expo Tuning 2026").fecha("2026-07-01").build());
        repository.save(Evento.builder().nombre("Trackday").fecha("2026-07-02").build());

        List<Evento> tuning = repository.findByNombreContainingIgnoreCase("tuning");
        assertEquals(1, tuning.size());
        assertEquals("Expo Tuning 2026", tuning.get(0).getNombre());
    }
}
