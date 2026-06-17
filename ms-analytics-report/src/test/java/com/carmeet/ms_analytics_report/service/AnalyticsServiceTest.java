package com.carmeet.ms_analytics_report.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.carmeet.ms_analytics_report.client.CompetitionRegClient;
import com.carmeet.ms_analytics_report.client.EventoCoreClient;
import com.carmeet.ms_analytics_report.client.TicketingClient;
import com.carmeet.ms_analytics_report.dto.MetricaDTO;
import com.carmeet.ms_analytics_report.model.Metrica;
import com.carmeet.ms_analytics_report.model.Reporte;
import com.carmeet.ms_analytics_report.repository.ReporteRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

    @Mock
    private ReporteRepository repo;

    @Mock
    private EventoCoreClient eventoClient;

    @Mock
    private TicketingClient ticketingClient;

    @Mock
    private CompetitionRegClient competitionClient;

    @InjectMocks
    private AnalyticsService service;

    // METODO: listar()
    @Test
    void listar_DebeRetornarTodosLosReportes() {
        // Arrange
        Reporte r1 = Reporte.builder().id(1L).totalTickets(5).build();
        Reporte r2 = Reporte.builder().id(2L).totalTickets(10).build();
        List<Reporte> reportesEsperados = Arrays.asList(r1, r2);

        when(repo.findAll()).thenReturn(reportesEsperados);

        // Act
        List<Reporte> resultado = service.listar();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(reportesEsperados, resultado);
        verify(repo, times(1)).findAll();
    }

    // METODO: obtenerPorId(Long id)
    @Test
    void obtenerPorId_CuandoExiste_DebeRetornarReporte() {
        // Arrange
        Long id = 1L;
        Reporte esperado = Reporte.builder().id(id).totalTickets(12).build();

        when(repo.findById(id)).thenReturn(Optional.of(esperado));

        // Act
        Reporte resultado = service.obtenerPorId(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        verify(repo, times(1)).findById(id);
    }

    @Test
    void obtenerPorId_CuandoNoExiste_DebeLanzarEntityNotFoundException() {
        // Arrange
        Long id = 99L;
        when(repo.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            service.obtenerPorId(id);
        });

        assertEquals("Reporte no encontrado con id: " + id, exception.getMessage());
        verify(repo, times(1)).findById(id);
    }

    // METODO: guardar(Reporte reporte)
    @Test
    void guardar_CuandoMetricasNoEsNulo_DebeAsociarMetricasYGuardar() {
        // Arrange
        Metrica m1 = Metrica.builder().nombre("t1").valor(5.0).build();
        List<Metrica> metricas = new ArrayList<>();
        metricas.add(m1);

        Reporte reporte = Reporte.builder().eventoId(1L).metricas(metricas).build();
        Reporte guardado = Reporte.builder().id(10L).eventoId(1L).metricas(metricas).build();

        when(repo.save(reporte)).thenReturn(guardado);

        // Act
        Reporte resultado = service.guardar(reporte);

        // Assert
        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals(reporte, m1.getReporte());
        verify(repo, times(1)).save(reporte);
    }

    @Test
    void guardar_CuandoMetricasEsNulo_DebeGuardarReporte() {
        // Arrange
        Reporte reporte = Reporte.builder().eventoId(1L).metricas(null).build();
        Reporte guardado = Reporte.builder().id(10L).eventoId(1L).metricas(null).build();

        when(repo.save(reporte)).thenReturn(guardado);

        // Act
        Reporte resultado = service.guardar(reporte);

        // Assert
        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        verify(repo, times(1)).save(reporte);
    }

    // METODO: actualizar(Long id, Reporte datosNuevos)
    @Test
    void actualizar_CuandoExisteYMetricasNoEsNulo_DebeActualizarYGuardar() {
        Long id = 1L;
        Reporte existente = new Reporte();
        existente.setId(id);
        existente.setMetricas(new ArrayList<>());

        Reporte datosNuevos = new Reporte();
        datosNuevos.setEventoId(20L);
        Metrica m = new Metrica();
        m.setNombre("TEST");
        datosNuevos.setMetricas(List.of(m));

        when(repo.findById(id)).thenReturn(Optional.of(existente));
        when(repo.save(existente)).thenReturn(existente);

        Reporte resultado = service.actualizar(id, datosNuevos);

        assertNotNull(resultado);
        assertEquals(20L, resultado.getEventoId());
        assertEquals(1, resultado.getMetricas().size());
        assertEquals(existente, m.getReporte());
        verify(repo, times(1)).save(existente);
    }

    @Test
    void actualizar_CuandoExisteYMetricasEsNulo_DebeActualizarSinMetricas() {
        Long id = 1L;
        Reporte existente = new Reporte();
        existente.setId(id);
        existente.setMetricas(new ArrayList<>());

        Reporte datosNuevos = new Reporte();
        datosNuevos.setEventoId(20L);
        datosNuevos.setMetricas(null);

        when(repo.findById(id)).thenReturn(Optional.of(existente));
        when(repo.save(existente)).thenReturn(existente);

        Reporte resultado = service.actualizar(id, datosNuevos);

        assertNotNull(resultado);
        assertEquals(20L, resultado.getEventoId());
        assertEquals(0, resultado.getMetricas().size());
        verify(repo, times(1)).save(existente);
    }

    @Test
    void actualizar_CuandoNoExiste_DebeLanzarEntityNotFoundException() {
        // Arrange
        Long id = 99L;
        Reporte datos = Reporte.builder().eventoId(10L).build();
        when(repo.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            service.actualizar(id, datos);
        });

        verify(repo, times(1)).findById(id);
        verify(repo, never()).save(any(Reporte.class));
    }

    // METODO: eliminar(Long id)
    @Test
    void eliminar_CuandoExiste_DebeEliminar() {
        // Arrange
        Long id = 1L;
        when(repo.existsById(id)).thenReturn(true);
        doNothing().when(repo).deleteById(id);

        // Act
        service.eliminar(id);

        // Assert
        verify(repo, times(1)).existsById(id);
        verify(repo, times(1)).deleteById(id);
    }

    @Test
    void eliminar_CuandoNoExiste_DebeLanzarEntityNotFoundException() {
        // Arrange
        Long id = 99L;
        when(repo.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            service.eliminar(id);
        });

        verify(repo, times(1)).existsById(id);
        verify(repo, never()).deleteById(anyLong());
    }

    // METODO: generarReporte(Long eventoId, String bearerToken)
    @Test
    void generarReporte_CuandoTodoFunciona_DebeGenerarReporteConMetricasYGuardar() {
        // Arrange
        Long eventoId = 5L;
        String token = "Bearer token";

        doNothing().when(eventoClient).validarEvento(eventoId, token);
        when(ticketingClient.contarTicketsPorEvento(eventoId, token)).thenReturn(50);
        when(competitionClient.contarInscripcionesPorEvento(eventoId, token)).thenReturn(25);
        when(repo.save(any(Reporte.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Reporte resultado = service.generarReporte(eventoId, token);

        // Assert
        assertNotNull(resultado);
        assertEquals(eventoId, resultado.getEventoId());
        assertEquals(1, resultado.getTotalEventos());
        assertEquals(50, resultado.getTotalTickets());
        assertEquals(25, resultado.getTotalInscripciones());
        assertNotNull(resultado.getFechaGeneracion());
        assertEquals(2, resultado.getMetricas().size());
        verify(eventoClient, times(1)).validarEvento(eventoId, token);
        verify(ticketingClient, times(1)).contarTicketsPorEvento(eventoId, token);
        verify(competitionClient, times(1)).contarInscripcionesPorEvento(eventoId, token);
        verify(repo, times(1)).save(any(Reporte.class));
    }

    @Test
    void generarReporte_CuandoClientesLanzanExcepcion_DebeGenerarReporteConCerosYGuardar() {
        // Arrange
        Long eventoId = 5L;
        String token = "Bearer token";

        doNothing().when(eventoClient).validarEvento(eventoId, token);
        when(ticketingClient.contarTicketsPorEvento(eventoId, token)).thenThrow(new RuntimeException("Ticketing Down"));
        when(competitionClient.contarInscripcionesPorEvento(eventoId, token)).thenThrow(new RuntimeException("Competition Down"));
        when(repo.save(any(Reporte.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Reporte resultado = service.generarReporte(eventoId, token);

        // Assert
        assertNotNull(resultado);
        assertEquals(0, resultado.getTotalTickets());
        assertEquals(0, resultado.getTotalInscripciones());
        assertEquals(2, resultado.getMetricas().size());
        verify(repo, times(1)).save(any(Reporte.class));
    }

    // METODO: resumenMetricas()
    @Test
    void resumenMetricas_DebeRetornarResumenCorrectoIncluyendoNulos() {
        Reporte r1 = new Reporte();
        Metrica m1 = new Metrica(1L, "T1", 10.0, r1);
        Metrica m2 = new Metrica(2L, "T1", 5.0, r1);
        Metrica mNulo = new Metrica(3L, "T1", null, r1);
        r1.setMetricas(List.of(m1, m2, mNulo));

        Reporte r2 = new Reporte();
        r2.setMetricas(null);

        Reporte r3 = new Reporte();
        Metrica m3 = new Metrica(4L, "T2", 20.0, r3);
        r3.setMetricas(List.of(m3));

        when(repo.findAll()).thenReturn(List.of(r1, r2, r3));

        List<MetricaDTO> resultado = service.resumenMetricas();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }
}
