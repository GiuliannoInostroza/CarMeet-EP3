package com.carmeet.ms_competition_reg.repository;

import com.carmeet.ms_competition_reg.model.Inscripcion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class InscripcionRepositoryTest {

    @Autowired
    private InscripcionRepository repository;

    @Test
    void debeGuardarInscripcion() {
        Inscripcion ins = Inscripcion.builder()
                .vehiculoId(10L)
                .eventoId(5L)
                .participante("Carlos")
                .categoria("Drift")
                .username("carlos_drift")
                .estado("PENDIENTE")
                .build();

        Inscripcion guardado = repository.save(ins);
        assertNotNull(guardado.getId());
        assertEquals("Carlos", guardado.getParticipante());
        assertEquals("PENDIENTE", guardado.getEstado());
    }

    @Test
    void debeBuscarInscripcionPorId() {
        Inscripcion ins = Inscripcion.builder()
                .vehiculoId(20L)
                .eventoId(6L)
                .participante("Maria")
                .categoria("Velocidad")
                .username("maria_v")
                .estado("APROBADA")
                .build();

        Inscripcion guardado = repository.save(ins);
        Optional<Inscripcion> resultado = repository.findById(guardado.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Maria", resultado.get().getParticipante());
    }

    @Test
    void debeListarInscripciones() {
        repository.save(Inscripcion.builder().vehiculoId(1L).eventoId(2L).participante("P1").username("u1").build());
        repository.save(Inscripcion.builder().vehiculoId(2L).eventoId(2L).participante("P2").username("u2").build());

        List<Inscripcion> resultado = repository.findAll();
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.size() >= 2);
    }

    @Test
    void debeEliminarInscripcion() {
        Inscripcion ins = Inscripcion.builder().vehiculoId(3L).eventoId(3L).participante("P3").username("u3").build();
        Inscripcion guardado = repository.save(ins);

        repository.deleteById(guardado.getId());
        Optional<Inscripcion> resultado = repository.findById(guardado.getId());
        assertFalse(resultado.isPresent());
    }

    @Test
    void debeBuscarPorEventoId() {
        repository.save(Inscripcion.builder().vehiculoId(1L).eventoId(100L).participante("A").username("ua").build());
        repository.save(Inscripcion.builder().vehiculoId(2L).eventoId(100L).participante("B").username("ub").build());
        repository.save(Inscripcion.builder().vehiculoId(3L).eventoId(200L).participante("C").username("uc").build());

        List<Inscripcion> filtrados = repository.findByEventoId(100L);
        assertEquals(2, filtrados.size());
    }

    @Test
    void debeBuscarPorVehiculoId() {
        repository.save(Inscripcion.builder().vehiculoId(50L).eventoId(1L).participante("A").username("ua").build());
        repository.save(Inscripcion.builder().vehiculoId(60L).eventoId(2L).participante("B").username("ub").build());

        List<Inscripcion> filtrados = repository.findByVehiculoId(50L);
        assertEquals(1, filtrados.size());
        assertEquals("A", filtrados.get(0).getParticipante());
    }

    @Test
    void debeBuscarPorUsername() {
        repository.save(Inscripcion.builder().vehiculoId(1L).eventoId(1L).participante("A").username("target_user").build());
        repository.save(Inscripcion.builder().vehiculoId(2L).eventoId(2L).participante("B").username("other_user").build());

        List<Inscripcion> filtrados = repository.findByUsername("target_user");
        assertEquals(1, filtrados.size());
        assertEquals("A", filtrados.get(0).getParticipante());
    }
}
