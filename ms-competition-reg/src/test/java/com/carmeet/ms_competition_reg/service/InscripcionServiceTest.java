package com.carmeet.ms_competition_reg.service;

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

import com.carmeet.ms_competition_reg.client.EventoCoreClient;
import com.carmeet.ms_competition_reg.client.NotificacionLogClient;
import com.carmeet.ms_competition_reg.client.VehiculoRegistryClient;
import com.carmeet.ms_competition_reg.model.Inscripcion;
import com.carmeet.ms_competition_reg.model.Requisito;
import com.carmeet.ms_competition_reg.repository.InscripcionRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class InscripcionServiceTest {

    @Mock
    private InscripcionRepository repo;

    @Mock
    private EventoCoreClient eventoClient;

    @Mock
    private VehiculoRegistryClient vehiculoClient;

    @Mock
    private NotificacionLogClient notificacionClient;

    @InjectMocks
    private InscripcionService service;

    // METODO: listar()
    @Test
    void listar_DebeRetornarTodasLasInscripciones() {
        // Arrange
        Inscripcion i1 = Inscripcion.builder().id(1L).participante("P1").build();
        Inscripcion i2 = Inscripcion.builder().id(2L).participante("P2").build();
        List<Inscripcion> list = Arrays.asList(i1, i2);

        when(repo.findAll()).thenReturn(list);

        // Act
        List<Inscripcion> resultado = service.listar();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(repo, times(1)).findAll();
    }

    // METODO: obtenerPorId(Long id)
    @Test
    void obtenerPorId_CuandoExiste_DebeRetornarInscripcion() {
        // Arrange
        Long id = 1L;
        Inscripcion ins = Inscripcion.builder().id(id).participante("John").build();
        when(repo.findById(id)).thenReturn(Optional.of(ins));

        // Act
        Inscripcion resultado = service.obtenerPorId(id);

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

    // METODO: guardar(Inscripcion inscripcion, String bearerToken)
    @Test
    void guardar_CuandoRequisitosNoEsNulo_DebeAsociarRequisitosDefinirPendienteYGuardar() {
        // Arrange
        String token = "Bearer token";
        Requisito r1 = Requisito.builder().nombre("Licencia").build();
        List<Requisito> reqs = new ArrayList<>();
        reqs.add(r1);

        Inscripcion insc = Inscripcion.builder().eventoId(10L).requisitos(reqs).build();
        Inscripcion guardada = Inscripcion.builder().id(1L).eventoId(10L).estado("PENDIENTE").requisitos(reqs).build();

        doNothing().when(eventoClient).validarEvento(10L, token);
        when(repo.save(insc)).thenReturn(guardada);

        // Act
        Inscripcion resultado = service.guardar(insc, token);

        // Assert
        assertNotNull(resultado);
        assertEquals("PENDIENTE", insc.getEstado());
        assertEquals(insc, r1.getInscripcion());
        verify(eventoClient, times(1)).validarEvento(10L, token);
        verify(repo, times(1)).save(insc);
    }

    @Test
    void guardar_CuandoRequisitosEsNulo_DebeGuardarSinModificarRequisitos() {
        String token = "Bearer token";
        Inscripcion insc = Inscripcion.builder().eventoId(10L).requisitos(null).build();
        Inscripcion guardada = Inscripcion.builder().id(1L).eventoId(10L).estado("PENDIENTE").requisitos(null).build();

        doNothing().when(eventoClient).validarEvento(10L, token);
        when(repo.save(insc)).thenReturn(guardada);

        Inscripcion resultado = service.guardar(insc, token);

        assertNotNull(resultado);
        assertEquals("PENDIENTE", insc.getEstado());
    }

    // METODO: actualizar(Long id, Inscripcion datosNuevos)
    @Test
    void actualizar_CuandoExisteYRequisitosNoEsNulo_DebeActualizarYGuardar() {
        // Arrange
        Long id = 1L;
        Requisito rOld = Requisito.builder().nombre("rOld").build();
        List<Requisito> lOld = new ArrayList<>();
        lOld.add(rOld);

        Inscripcion existente = Inscripcion.builder()
                .id(id)
                .vehiculoId(2L)
                .eventoId(3L)
                .participante("Antiguo")
                .categoria("Novato")
                .username("userOld")
                .requisitos(lOld)
                .build();
        rOld.setInscripcion(existente);

        Requisito rNew = Requisito.builder().nombre("rNew").build();
        List<Requisito> lNew = new ArrayList<>();
        lNew.add(rNew);

        Inscripcion datosNuevos = Inscripcion.builder()
                .vehiculoId(4L)
                .eventoId(5L)
                .participante("Nuevo")
                .categoria("Pro")
                .username("userNew")
                .requisitos(lNew)
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(existente));
        when(repo.save(existente)).thenReturn(existente);

        // Act
        Inscripcion resultado = service.actualizar(id, datosNuevos);

        // Assert
        assertNotNull(resultado);
        assertEquals(4L, resultado.getVehiculoId());
        assertEquals(5L, resultado.getEventoId());
        assertEquals("Nuevo", resultado.getParticipante());
        assertEquals("Pro", resultado.getCategoria());
        assertEquals("userNew", resultado.getUsername());
        assertEquals(1, resultado.getRequisitos().size());
        assertEquals(rNew, resultado.getRequisitos().get(0));
        assertEquals(existente, rNew.getInscripcion());
    }

    @Test
    void actualizar_CuandoExisteYRequisitosEsNulo_DebeActualizarYGuardarSinModificarNuevosRequisitos() {
        Long id = 1L;
        Inscripcion existente = Inscripcion.builder().id(id).requisitos(new ArrayList<>()).build();
        Inscripcion datosNuevos = Inscripcion.builder().vehiculoId(4L).requisitos(null).build();

        when(repo.findById(id)).thenReturn(Optional.of(existente));
        when(repo.save(existente)).thenReturn(existente);

        Inscripcion resultado = service.actualizar(id, datosNuevos);

        assertNotNull(resultado);
        assertEquals(4L, resultado.getVehiculoId());
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

    // METODO: obtenerPorEventoId(Long eventoId)
    @Test
    void obtenerPorEventoId_DebeRetornarInscripciones() {
        // Arrange
        Long evId = 1L;
        List<Inscripcion> list = List.of(new Inscripcion());
        when(repo.findByEventoId(evId)).thenReturn(list);

        // Act
        List<Inscripcion> resultado = service.obtenerPorEventoId(evId);

        // Assert
        assertEquals(1, resultado.size());
    }

    // METODO: obtenerPorVehiculoId(Long vehiculoId)
    @Test
    void obtenerPorVehiculoId_DebeRetornarInscripciones() {
        // Arrange
        Long vehId = 2L;
        List<Inscripcion> list = List.of(new Inscripcion());
        when(repo.findByVehiculoId(vehId)).thenReturn(list);

        // Act
        List<Inscripcion> resultado = service.obtenerPorVehiculoId(vehId);

        // Assert
        assertEquals(1, resultado.size());
    }

    // METODO: aprobar(Long id, String bearerToken)
    @Test
    void aprobar_CuandoEsPendienteYConVehiculoYUsername_DebeAprobarYNotificar() {
        // Arrange
        Long id = 1L;
        String token = "Bearer token";
        Inscripcion insc = Inscripcion.builder()
                .id(id)
                .estado("PENDIENTE")
                .vehiculoId(5L)
                .username("john")
                .categoria("Pro")
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(insc));
        doNothing().when(vehiculoClient).validarVehiculo(5L, token);
        when(repo.save(insc)).thenReturn(insc);
        doNothing().when(notificacionClient).enviar(eq("john"), anyString(), eq(token));

        // Act
        Inscripcion resultado = service.aprobar(id, token);

        // Assert
        assertNotNull(resultado);
        assertEquals("APROBADA", resultado.getEstado());
        verify(vehiculoClient, times(1)).validarVehiculo(5L, token);
        verify(notificacionClient, times(1)).enviar(eq("john"), anyString(), eq(token));
    }

    @Test
    void aprobar_CuandoEsPendienteYVehiculoYUsernameSonNulos_DebeAprobarSinNotificarNiValidar() {
        Long id = 1L;
        String token = "Bearer token";
        Inscripcion insc = Inscripcion.builder().id(id).estado("PENDIENTE").vehiculoId(null).username(null).build();

        when(repo.findById(id)).thenReturn(Optional.of(insc));
        when(repo.save(insc)).thenReturn(insc);

        Inscripcion resultado = service.aprobar(id, token);

        assertNotNull(resultado);
        assertEquals("APROBADA", resultado.getEstado());
        verify(vehiculoClient, never()).validarVehiculo(anyLong(), anyString());
        verify(notificacionClient, never()).enviar(anyString(), anyString(), anyString());
    }

    @Test
    void aprobar_CuandoNoEstaPendiente_DebeLanzarRuntimeException() {
        // Arrange
        Long id = 1L;
        Inscripcion insc = Inscripcion.builder().id(id).estado("APROBADA").build();
        when(repo.findById(id)).thenReturn(Optional.of(insc));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            service.aprobar(id, "token");
        });
        verify(repo, never()).save(any(Inscripcion.class));
    }

    // METODO: rechazar(Long id, String bearerToken)
    @Test
    void rechazar_CuandoEsPendienteYConUsername_DebeRechazarYNotificar() {
        // Arrange
        Long id = 1L;
        String token = "Bearer token";
        Inscripcion insc = Inscripcion.builder()
                .id(id)
                .estado("PENDIENTE")
                .username("john")
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(insc));
        when(repo.save(insc)).thenReturn(insc);
        doNothing().when(notificacionClient).enviar(eq("john"), anyString(), eq(token));

        // Act
        Inscripcion resultado = service.rechazar(id, token);

        // Assert
        assertNotNull(resultado);
        assertEquals("RECHAZADA", resultado.getEstado());
        verify(notificacionClient, times(1)).enviar(eq("john"), anyString(), eq(token));
    }

    @Test
    void rechazar_CuandoEsPendienteYUsernameEsNulo_DebeRechazarSinNotificar() {
        Long id = 1L;
        String token = "Bearer token";
        Inscripcion insc = Inscripcion.builder().id(id).estado("PENDIENTE").username(null).build();

        when(repo.findById(id)).thenReturn(Optional.of(insc));
        when(repo.save(insc)).thenReturn(insc);

        Inscripcion resultado = service.rechazar(id, token);

        assertNotNull(resultado);
        assertEquals("RECHAZADA", resultado.getEstado());
        verify(notificacionClient, never()).enviar(anyString(), anyString(), anyString());
    }

    @Test
    void rechazar_CuandoNoEstaPendiente_DebeLanzarRuntimeException() {
        // Arrange
        Long id = 1L;
        Inscripcion insc = Inscripcion.builder().id(id).estado("RECHAZADA").build();
        when(repo.findById(id)).thenReturn(Optional.of(insc));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            service.rechazar(id, "token");
        });
    }
}
