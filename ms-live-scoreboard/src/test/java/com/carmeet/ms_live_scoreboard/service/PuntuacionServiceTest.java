package com.carmeet.ms_live_scoreboard.service;

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

import com.carmeet.ms_live_scoreboard.client.CompetitionRegClient;
import com.carmeet.ms_live_scoreboard.model.Puntuacion;
import com.carmeet.ms_live_scoreboard.model.DetallePuntuacion;
import com.carmeet.ms_live_scoreboard.repository.PuntuacionRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class PuntuacionServiceTest {

    @Mock
    private PuntuacionRepository repo;

    @Mock
    private CompetitionRegClient competitionClient;

    @InjectMocks
    private PuntuacionService service;

    // METODO: listar()
    @Test
    void listar_DebeRetornarTodasLasPuntuaciones() {
        // Arrange
        Puntuacion p1 = Puntuacion.builder().id(1L).puntos(95).build();
        Puntuacion p2 = Puntuacion.builder().id(2L).puntos(88).build();
        List<Puntuacion> list = Arrays.asList(p1, p2);

        when(repo.findAll()).thenReturn(list);

        // Act
        List<Puntuacion> resultado = service.listar();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(repo, times(1)).findAll();
    }

    // METODO: obtenerPorId(Long id)
    @Test
    void obtenerPorId_CuandoExiste_DebeRetornarPuntuacion() {
        // Arrange
        Long id = 1L;
        Puntuacion p = Puntuacion.builder().id(id).puntos(100).build();
        when(repo.findById(id)).thenReturn(Optional.of(p));

        // Act
        Puntuacion resultado = service.obtenerPorId(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
    }

    @Test
    void obtenerPorId_CuandoNoExiste_DebeLanzarEntityNotFoundException() {
        // Arrange
        Long id = 99L;
        when(repo.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            service.obtenerPorId(id);
        });
    }

    // METODO: guardar(Puntuacion puntuacion, String bearerToken)
    @Test
    void guardar_CuandoDetallesNoEsNulo_DebeAsociarDetallesYGuardar() {
        // Arrange
        String token = "Bearer token";
        DetallePuntuacion d = DetallePuntuacion.builder().categoria("Estilo").puntosAsignados(20).build();
        List<DetallePuntuacion> dets = new ArrayList<>();
        dets.add(d);

        Puntuacion p = Puntuacion.builder().inscripcionId(10L).detalles(dets).build();
        Puntuacion guardada = Puntuacion.builder().id(1L).inscripcionId(10L).detalles(dets).build();

        doNothing().when(competitionClient).validarInscripcion(10L, token);
        when(repo.save(p)).thenReturn(guardada);

        // Act
        Puntuacion resultado = service.guardar(p, token);

        // Assert
        assertNotNull(resultado);
        assertEquals(p, d.getPuntuacion());
        verify(competitionClient, times(1)).validarInscripcion(10L, token);
        verify(repo, times(1)).save(p);
    }

    @Test
    void guardar_CuandoDetallesEsNulo_DebeGuardarSinModificarDetalles() {
        String token = "Bearer token";
        Puntuacion p = Puntuacion.builder().inscripcionId(10L).detalles(null).build();
        Puntuacion guardada = Puntuacion.builder().id(1L).inscripcionId(10L).detalles(null).build();

        doNothing().when(competitionClient).validarInscripcion(10L, token);
        when(repo.save(p)).thenReturn(guardada);

        Puntuacion resultado = service.guardar(p, token);

        assertNotNull(resultado);
        verify(repo, times(1)).save(p);
    }

    // METODO: actualizar(Long id, Puntuacion datosNuevos)
    @Test
    void actualizar_CuandoExisteYDetallesNoEsNulo_DebeActualizarYGuardar() {
        // Arrange
        Long id = 1L;
        DetallePuntuacion dOld = DetallePuntuacion.builder().categoria("Old").puntosAsignados(10).build();
        List<DetallePuntuacion> lOld = new ArrayList<>();
        lOld.add(dOld);

        Puntuacion existente = Puntuacion.builder()
                .id(id)
                .inscripcionId(2L)
                .eventoId(3L)
                .puntos(50)
                .detalles(lOld)
                .build();
        dOld.setPuntuacion(existente);

        DetallePuntuacion dNew = DetallePuntuacion.builder().categoria("New").puntosAsignados(15).build();
        List<DetallePuntuacion> lNew = new ArrayList<>();
        lNew.add(dNew);

        Puntuacion datosNuevos = Puntuacion.builder()
                .inscripcionId(4L)
                .eventoId(5L)
                .puntos(75)
                .detalles(lNew)
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(existente));
        when(repo.save(existente)).thenReturn(existente);

        // Act
        Puntuacion resultado = service.actualizar(id, datosNuevos);

        // Assert
        assertNotNull(resultado);
        assertEquals(4L, resultado.getInscripcionId());
        assertEquals(5L, resultado.getEventoId());
        assertEquals(Integer.valueOf(75), resultado.getPuntos());
        assertEquals(1, resultado.getDetalles().size());
        assertEquals(dNew, resultado.getDetalles().get(0));
        assertEquals(existente, dNew.getPuntuacion());
    }

    @Test
    void actualizar_CuandoExisteYDetallesEsNulo_DebeActualizarYGuardarSinNuevosDetalles() {
        Long id = 1L;
        Puntuacion existente = Puntuacion.builder().id(id).detalles(new ArrayList<>()).build();
        Puntuacion datosNuevos = Puntuacion.builder().puntos(75).detalles(null).build();

        when(repo.findById(id)).thenReturn(Optional.of(existente));
        when(repo.save(existente)).thenReturn(existente);

        Puntuacion resultado = service.actualizar(id, datosNuevos);

        assertNotNull(resultado);
        assertEquals(Integer.valueOf(75), resultado.getPuntos());
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
    }

    // METODO: rankingPorEvento(Long eventoId)
    @Test
    void rankingPorEvento_DebeRetornarRanking() {
        // Arrange
        Long evId = 5L;
        List<Puntuacion> list = List.of(new Puntuacion());
        when(repo.findByEventoIdOrderByPuntosDesc(evId)).thenReturn(list);

        // Act
        List<Puntuacion> resultado = service.rankingPorEvento(evId);

        // Assert
        assertEquals(1, resultado.size());
    }

    // METODO: porInscripcion(Long inscripcionId)
    @Test
    void porInscripcion_DebeRetornarPuntuacion() {
        // Arrange
        Long insId = 10L;
        List<Puntuacion> list = List.of(new Puntuacion());
        when(repo.findByInscripcionId(insId)).thenReturn(list);

        // Act
        List<Puntuacion> resultado = service.porInscripcion(insId);

        // Assert
        assertEquals(1, resultado.size());
    }

    // METODO: top10Global()
    @Test
    void top10Global_DebeRetornarTop10() {
        // Arrange
        List<Puntuacion> list = List.of(new Puntuacion());
        when(repo.findTop10ByOrderByPuntosDesc()).thenReturn(list);

        // Act
        List<Puntuacion> resultado = service.top10Global();

        // Assert
        assertEquals(1, resultado.size());
    }
}
