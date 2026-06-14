package com.carmeet.ms_analytics_report.repository;

import com.carmeet.ms_analytics_report.model.Reporte;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ReporteRepositoryTest {

    @Autowired
    private ReporteRepository repository;

    @Test
    void debeGuardarReporte() {
        Reporte r = Reporte.builder()
                .eventoId(1L)
                .totalEventos(5)
                .totalTickets(100)
                .totalInscripciones(50)
                .fechaGeneracion("2026-06-14")
                .build();
        Reporte guardado = repository.save(r);
        assertNotNull(guardado.getId());
        assertEquals(1L, guardado.getEventoId());
        assertEquals(100, guardado.getTotalTickets());
    }

    @Test
    void debeBuscarReportePorId() {
        Reporte r = Reporte.builder()
                .eventoId(2L)
                .totalEventos(3)
                .totalTickets(40)
                .totalInscripciones(10)
                .fechaGeneracion("2026-06-15")
                .build();
        Reporte guardado = repository.save(r);
        Optional<Reporte> resultado = repository.findById(guardado.getId());
        assertTrue(resultado.isPresent());
        assertEquals(3, resultado.get().getTotalEventos());
    }

    @Test
    void debeListarReportes() {
        repository.save(Reporte.builder().eventoId(10L).build());
        repository.save(Reporte.builder().eventoId(20L).build());
        List<Reporte> resultado = repository.findAll();
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.size() >= 2);
    }

    @Test
    void debeEliminarReporte() {
        Reporte r = Reporte.builder().eventoId(30L).build();
        Reporte guardado = repository.save(r);
        repository.deleteById(guardado.getId());
        Optional<Reporte> resultado = repository.findById(guardado.getId());
        assertFalse(resultado.isPresent());
    }
}
