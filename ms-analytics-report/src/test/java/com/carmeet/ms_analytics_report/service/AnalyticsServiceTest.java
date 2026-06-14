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
    void actualizar_CuandoExisteYMetricasNoEsNulo_DebeActualizarLimpiarMetricasYGuardar() {
        // Arrange
        Long id = 1L;
        Metrica mAntigua = Metrica.builder().nombre("mOld").valor(1.0).build();
        List<Metrica> listaAntigua = new ArrayList<>();
        listaAntigua.add(mAntigua);

        Reporte existente = Reporte.builder()
                .id(id)
                .eventoId(2L)
                .totalEventos(1)
                .totalTickets(10)
                .totalInscripciones(5)
                .fechaGeneracion("2026-06-12")
                .metricas(listaAntigua)
                .build();
        mAntigua.setReporte(existente);

        Metrica mNueva = Metrica.builder().nombre("mNew").valor(2.0).build();
        List<Metrica> listaNueva = new ArrayList<>();
        listaNueva.add(mNueva);

        Reporte datosNuevos = Reporte.builder()
                .eventoId(3L)
                .totalEventos(2)
                .totalTickets(20)
                .totalInscripciones(10)
                .fechaGeneracion("2026-06-13")
                .metricas(listaNueva)
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(existente));
        when(repo.save(existente)).thenReturn(existente);

        // Act
        Reporte resultado = service.actualizar(id, datosNuevos);

        // Assert
        assertNotNull(resultado);
        assertEquals(3L, resultado.getEventoId());
        assertEquals(2, resultado.getTotalEventos());
        assertEquals(20, resultado.getTotalTickets());
        assertEquals(10, resultado.getTotalInscripciones());
        assertEquals("2026-06-13", resultado.getFechaGeneracion());
        assertEquals(1, resultado.getMetricas().size());
        assertEquals(mNueva, resultado.getMetricas().get(0));
        assertEquals(existente, mNueva.getReporte());
        verify(repo, times(1)).findById(id);
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
    void resumenMetricas_DebeRetornarMetricasAgrupadasPorNombre() {
        // Arrange
        Metrica m1 = Metrica.builder().nombre("total_tickets").valor(10.0).build();
        Metrica m2 = Metrica.builder().nombre("total_tickets").valor(20.0).build();
        Metrica m3 = Metrica.builder().nombre("total_inscripciones").valor(5.0).build();

        Reporte r1 = Reporte.builder().metricas(Arrays.asList(m1, m3)).build();
        Reporte r2 = Reporte.builder().metricas(List.of(m2)).build();

        when(repo.findAll()).thenReturn(Arrays.asList(r1, r2));

        // Act
        List<MetricaDTO> resultado = service.resumenMetricas();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        
        MetricaDTO ticketsDTO = resultado.stream()
                .filter(dto -> "total_tickets".equals(dto.getNombre()))
                .findFirst().orElse(null);
        
        MetricaDTO inscripcionesDTO = resultado.stream()
                .filter(dto -> "total_inscripciones".equals(dto.getNombre()))
                .findFirst().orElse(null);

        assertNotNull(ticketsDTO);
        assertEquals(30.0, ticketsDTO.getValor());
        assertNotNull(inscripcionesDTO);
        assertEquals(5.0, inscripcionesDTO.getValor());
    }
}
