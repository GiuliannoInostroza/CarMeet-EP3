package com.carmeet.ms_event_core.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.carmeet.ms_event_core.model.Evento;
import com.carmeet.ms_event_core.model.Patrocinador;
import com.carmeet.ms_event_core.repository.EventoRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class EventoServiceTest {

    @Mock
    private EventoRepository repo;

    @InjectMocks
    private EventoService service;

    // METODO: listar()
    @Test
    void listar_DebeRetornarTodosLosEventos() {
        // Arrange
        Evento e1 = Evento.builder().id(1L).nombre("Event 1").build();
        Evento e2 = Evento.builder().id(2L).nombre("Event 2").build();
        List<Evento> list = Arrays.asList(e1, e2);

        when(repo.findAll()).thenReturn(list);

        // Act
        List<Evento> resultado = service.listar();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(repo, times(1)).findAll();
    }

    // METODO: obtenerPorId(Long id)
    @Test
    void obtenerPorId_CuandoExiste_DebeRetornarEvento() {
        // Arrange
        Long id = 1L;
        Evento e = Evento.builder().id(id).nombre("Event").build();
        when(repo.findById(id)).thenReturn(Optional.of(e));

        // Act
        Evento resultado = service.obtenerPorId(id);

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

    // METODO: guardar(Evento evento)
    @Test
    void guardar_CuandoPatrocinadoresNoEsNulo_DebeAsociarPatrocinadoresYGuardar() {
        // Arrange
        Patrocinador p = Patrocinador.builder().nombre("RedBull").build();
        List<Patrocinador> patrs = new ArrayList<>();
        patrs.add(p);

        Evento e = Evento.builder().nombre("Car Meet").patrocinadores(patrs).build();
        Evento guardado = Evento.builder().id(1L).nombre("Car Meet").patrocinadores(patrs).build();

        when(repo.save(e)).thenReturn(guardado);

        // Act
        Evento resultado = service.guardar(e);

        // Assert
        assertNotNull(resultado);
        assertEquals(e, p.getEvento());
        verify(repo, times(1)).save(e);
    }

    @Test
    void guardar_CuandoPatrocinadoresEsNulo_DebeGuardarSinModificarPatrocinadores() {
        Evento e = Evento.builder().nombre("Car Meet").patrocinadores(null).build();
        Evento guardado = Evento.builder().id(1L).nombre("Car Meet").patrocinadores(null).build();

        when(repo.save(e)).thenReturn(guardado);

        Evento resultado = service.guardar(e);

        assertNotNull(resultado);
        verify(repo, times(1)).save(e);
    }

    // METODO: actualizar(Long id, Evento datosNuevos)
    @Test
    void actualizar_CuandoExisteYPatrocinadoresNoEsNulo_DebeActualizarYGuardar() {
        // Arrange
        Long id = 1L;
        Patrocinador pOld = Patrocinador.builder().nombre("Old Sponsor").build();
        List<Patrocinador> lOld = new ArrayList<>();
        lOld.add(pOld);

        Evento existente = Evento.builder()
                .id(id)
                .nombre("Old Name")
                .fecha("2026-06-12")
                .ubicacion("Old Track")
                .patrocinadores(lOld)
                .build();
        pOld.setEvento(existente);

        Patrocinador pNew = Patrocinador.builder().nombre("New Sponsor").build();
        List<Patrocinador> lNew = new ArrayList<>();
        lNew.add(pNew);

        Evento datosNuevos = Evento.builder()
                .nombre("New Name")
                .fecha("2026-06-15")
                .ubicacion("New Track")
                .patrocinadores(lNew)
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(existente));
        when(repo.save(existente)).thenReturn(existente);

        // Act
        Evento resultado = service.actualizar(id, datosNuevos);

        // Assert
        assertNotNull(resultado);
        assertEquals("New Name", resultado.getNombre());
        assertEquals("2026-06-15", resultado.getFecha());
        assertEquals("New Track", resultado.getUbicacion());
        assertEquals(1, resultado.getPatrocinadores().size());
        assertEquals(pNew, resultado.getPatrocinadores().get(0));
        assertEquals(existente, pNew.getEvento());
    }

    @Test
    void actualizar_CuandoExisteYPatrocinadoresEsNulo_DebeActualizarYGuardarSinNuevosPatrocinadores() {
        Long id = 1L;
        Evento existente = Evento.builder().id(id).patrocinadores(new ArrayList<>()).build();
        Evento datosNuevos = Evento.builder().nombre("New").patrocinadores(null).build();

        when(repo.findById(id)).thenReturn(Optional.of(existente));
        when(repo.save(existente)).thenReturn(existente);

        Evento resultado = service.actualizar(id, datosNuevos);

        assertNotNull(resultado);
        assertEquals("New", resultado.getNombre());
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

    // METODO: listarProximos()
    @Test
    void listarProximos_DebeRetornarEventosProximos() {
        // Arrange
        String hoy = LocalDate.now().toString();
        List<Evento> list = List.of(new Evento());
        when(repo.findEventosProximos(hoy)).thenReturn(list);

        // Act
        List<Evento> resultado = service.listarProximos();

        // Assert
        assertEquals(1, resultado.size());
    }

    // METODO: buscarPorNombre(String nombre)
    @Test
    void buscarPorNombre_CuandoNombreEsNuloOBlanco_DebeLanzarRuntimeException() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            service.buscarPorNombre(null);
        });
        assertThrows(RuntimeException.class, () -> {
            service.buscarPorNombre("   ");
        });
    }

    @Test
    void buscarPorNombre_CuandoNombreEsValido_DebeRetornarResultados() {
        // Arrange
        String query = "meet";
        List<Evento> list = List.of(new Evento());
        when(repo.findByNombreContainingIgnoreCase(query)).thenReturn(list);

        // Act
        List<Evento> resultado = service.buscarPorNombre(query);

        // Assert
        assertEquals(1, resultado.size());
    }
}
