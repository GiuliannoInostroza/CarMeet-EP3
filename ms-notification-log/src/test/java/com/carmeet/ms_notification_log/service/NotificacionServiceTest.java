package com.carmeet.ms_notification_log.service;

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

import com.carmeet.ms_notification_log.model.Notificacion;
import com.carmeet.ms_notification_log.model.Adjunto;
import com.carmeet.ms_notification_log.repository.NotificacionRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class NotificacionServiceTest {

    @Mock
    private NotificacionRepository repo;

    @InjectMocks
    private NotificacionService service;

    // METODO: listar()
    @Test
    void listar_DebeRetornarTodasLasNotificaciones() {
        // Arrange
        Notificacion n1 = Notificacion.builder().id(1L).destinatario("john").build();
        Notificacion n2 = Notificacion.builder().id(2L).destinatario("jane").build();
        List<Notificacion> list = Arrays.asList(n1, n2);

        when(repo.findAll()).thenReturn(list);

        // Act
        List<Notificacion> resultado = service.listar();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(repo, times(1)).findAll();
    }

    // METODO: obtenerPorId(Long id)
    @Test
    void obtenerPorId_CuandoExiste_DebeRetornarNotificacion() {
        // Arrange
        Long id = 1L;
        Notificacion n = Notificacion.builder().id(id).destinatario("john").build();
        when(repo.findById(id)).thenReturn(Optional.of(n));

        // Act
        Notificacion resultado = service.obtenerPorId(id);

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

    // METODO: guardar(Notificacion notificacion)
    @Test
    void guardar_CuandoAdjuntosNoEsNuloYLeidaEsNulo_DebeAsociarAdjuntosPonerFalsaLeidaYGuardar() {
        // Arrange
        Adjunto a = Adjunto.builder().nombreArchivo("doc.pdf").build();
        List<Adjunto> adjs = new ArrayList<>();
        adjs.add(a);

        Notificacion n = Notificacion.builder().destinatario("john").adjuntos(adjs).leida(null).build();
        Notificacion guardada = Notificacion.builder().id(1L).destinatario("john").adjuntos(adjs).leida(false).build();

        when(repo.save(n)).thenReturn(guardada);

        // Act
        Notificacion resultado = service.guardar(n);

        // Assert
        assertNotNull(resultado);
        assertEquals(n, a.getNotificacion());
        assertFalse(n.getLeida());
        verify(repo, times(1)).save(n);
    }

    // METODO: actualizar(Long id, Notificacion datosNuevos)
    @Test
    void actualizar_CuandoExisteYAdjuntosNoEsNulo_DebeActualizarYGuardar() {
        // Arrange
        Long id = 1L;
        Adjunto aOld = Adjunto.builder().nombreArchivo("old.pdf").build();
        List<Adjunto> lOld = new ArrayList<>();
        lOld.add(aOld);

        Notificacion existente = Notificacion.builder()
                .id(id)
                .destinatario("old")
                .mensaje("old message")
                .adjuntos(lOld)
                .build();
        aOld.setNotificacion(existente);

        Adjunto aNew = Adjunto.builder().nombreArchivo("new.pdf").build();
        List<Adjunto> lNew = new ArrayList<>();
        lNew.add(aNew);

        Notificacion datosNuevos = Notificacion.builder()
                .destinatario("new")
                .mensaje("new message")
                .adjuntos(lNew)
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(existente));
        when(repo.save(existente)).thenReturn(existente);

        // Act
        Notificacion resultado = service.actualizar(id, datosNuevos);

        // Assert
        assertNotNull(resultado);
        assertEquals("new", resultado.getDestinatario());
        assertEquals("new message", resultado.getMensaje());
        assertEquals(1, resultado.getAdjuntos().size());
        assertEquals(aNew, resultado.getAdjuntos().get(0));
        assertEquals(existente, aNew.getNotificacion());
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

    // METODO: obtenerPorDestinatario(String username)
    @Test
    void obtenerPorDestinatario_DebeRetornarNotificaciones() {
        // Arrange
        String username = "john";
        List<Notificacion> list = List.of(new Notificacion());
        when(repo.findByDestinatario(username)).thenReturn(list);

        // Act
        List<Notificacion> resultado = service.obtenerPorDestinatario(username);

        // Assert
        assertEquals(1, resultado.size());
    }

    // METODO: enviar(Notificacion notificacion)
    @Test
    void enviar_CuandoFaltaDestinatarioOMensaje_DebeLanzarRuntimeException() {
        // Arrange
        Notificacion n1 = Notificacion.builder().destinatario(null).mensaje("hola").build();
        Notificacion n2 = Notificacion.builder().destinatario("john").mensaje("  ").build();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            service.enviar(n1);
        });
        assertThrows(RuntimeException.class, () -> {
            service.enviar(n2);
        });
    }

    @Test
    void enviar_CuandoDatosValidos_DebeMarcarNoLeidaGuardarYRetornar() {
        // Arrange
        Notificacion n = Notificacion.builder().destinatario("john").mensaje("hola").build();
        when(repo.save(n)).thenReturn(n);

        // Act
        Notificacion resultado = service.enviar(n);

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.getLeida());
        verify(repo, times(1)).save(n);
    }

    // METODO: marcarLeida(Long id)
    @Test
    void marcarLeida_CuandoYaEstaLeida_DebeLanzarRuntimeException() {
        // Arrange
        Long id = 1L;
        Notificacion n = Notificacion.builder().id(id).leida(true).build();
        when(repo.findById(id)).thenReturn(Optional.of(n));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            service.marcarLeida(id);
        });
    }

    @Test
    void marcarLeida_CuandoNoEstaLeida_DebeCambiarALeidaYGuardar() {
        // Arrange
        Long id = 1L;
        Notificacion n = Notificacion.builder().id(id).leida(false).build();
        when(repo.findById(id)).thenReturn(Optional.of(n));
        when(repo.save(n)).thenReturn(n);

        // Act
        Notificacion resultado = service.marcarLeida(id);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.getLeida());
        verify(repo, times(1)).save(n);
    }
}
