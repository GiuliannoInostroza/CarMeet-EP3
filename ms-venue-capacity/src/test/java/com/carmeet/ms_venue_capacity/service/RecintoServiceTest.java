package com.carmeet.ms_venue_capacity.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.carmeet.ms_venue_capacity.model.Recinto;
import com.carmeet.ms_venue_capacity.model.Zona;
import com.carmeet.ms_venue_capacity.repository.RecintoRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class RecintoServiceTest {

    @Mock
    private RecintoRepository repo;

    @InjectMocks
    private RecintoService service;

    // METODO: listar()
    @Test
    void listar_DebeRetornarTodosLosRecintos() {
        // Arrange
        Recinto r1 = Recinto.builder().id(1L).nombre("R1").build();
        Recinto r2 = Recinto.builder().id(2L).nombre("R2").build();
        List<Recinto> list = Arrays.asList(r1, r2);

        when(repo.findAll()).thenReturn(list);

        // Act
        List<Recinto> resultado = service.listar();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(repo, times(1)).findAll();
    }

    // METODO: obtenerPorId(Long id)
    @Test
    void obtenerPorId_CuandoExiste_DebeRetornarRecinto() {
        // Arrange
        Long id = 1L;
        Recinto r = Recinto.builder().id(id).nombre("R1").build();
        when(repo.findById(id)).thenReturn(Optional.of(r));

        // Act
        Recinto resultado = service.obtenerPorId(id);

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

    // METODO: guardar(Recinto recinto)
    @Test
    void guardar_CuandoZonasNoEsNulo_DebeAsociarZonasYGuardar() {
        // Arrange
        Zona z = Zona.builder().nombre("Pits").build();
        List<Zona> zonas = new ArrayList<>();
        zonas.add(z);

        Recinto r = Recinto.builder().nombre("Autodromo").zonas(zonas).build();
        Recinto guardado = Recinto.builder().id(1L).nombre("Autodromo").zonas(zonas).build();

        when(repo.save(r)).thenReturn(guardado);

        // Act
        Recinto resultado = service.guardar(r);

        // Assert
        assertNotNull(resultado);
        assertEquals(r, z.getRecinto());
        verify(repo, times(1)).save(r);
    }

    // METODO: actualizar(Long id, Recinto datosNuevos)
    @Test
    void actualizar_CuandoExisteYZonasNoEsNulo_DebeActualizarYGuardar() {
        // Arrange
        Long id = 1L;
        Zona zOld = Zona.builder().nombre("Old zone").build();
        List<Zona> lOld = new ArrayList<>();
        lOld.add(zOld);

        Recinto existente = Recinto.builder()
                .id(id)
                .nombre("Old Name")
                .capacidadMaxima(100)
                .ocupacionActual(50)
                .zonas(lOld)
                .build();
        zOld.setRecinto(existente);

        Zona zNew = Zona.builder().nombre("New zone").build();
        List<Zona> lNew = new ArrayList<>();
        lNew.add(zNew);

        Recinto datosNuevos = Recinto.builder()
                .nombre("New Name")
                .capacidadMaxima(200)
                .ocupacionActual(60)
                .zonas(lNew)
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(existente));
        when(repo.save(existente)).thenReturn(existente);

        // Act
        Recinto resultado = service.actualizar(id, datosNuevos);

        // Assert
        assertNotNull(resultado);
        assertEquals("New Name", resultado.getNombre());
        assertEquals(200, resultado.getCapacidadMaxima());
        assertEquals(60, resultado.getOcupacionActual());
        assertEquals(1, resultado.getZonas().size());
        assertEquals(zNew, resultado.getZonas().get(0));
        assertEquals(existente, zNew.getRecinto());
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

    // METODO: listarZonas(Long recintoId)
    @Test
    void listarZonas_DebeRetornarListaDeZonas() {
        // Arrange
        Long rId = 1L;
        Zona z = new Zona();
        List<Zona> list = List.of(z);
        Recinto r = Recinto.builder().id(rId).zonas(list).build();

        when(repo.findById(rId)).thenReturn(Optional.of(r));

        // Act
        List<Zona> resultado = service.listarZonas(rId);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(list, resultado);
    }

    // METODO: consultarDisponibilidad(Long id)
    @Test
    void consultarDisponibilidad_DebeRetornarMapaDeInformacion() {
        // Arrange
        Long id = 1L;
        Recinto r = Recinto.builder()
                .id(id)
                .nombre("Autodromo")
                .capacidadMaxima(100)
                .ocupacionActual(30)
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(r));

        // Act
        Map<String, Object> resultado = service.consultarDisponibilidad(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(id, resultado.get("recintoId"));
        assertEquals("Autodromo", resultado.get("nombre"));
        assertEquals(true, resultado.get("disponible"));
        assertEquals(100, resultado.get("capacidadMaxima"));
        assertEquals(30, resultado.get("ocupacionActual"));
        assertEquals(70, resultado.get("plazasLibres"));
    }

    // METODO: registrarIngreso(Long id)
    @Test
    void registrarIngreso_CuandoRecintoEstaLleno_DebeLanzarRuntimeException() {
        // Arrange
        Long id = 1L;
        Recinto r = Recinto.builder().id(id).capacidadMaxima(100).ocupacionActual(100).build();
        when(repo.findById(id)).thenReturn(Optional.of(r));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            service.registrarIngreso(id);
        });
        verify(repo, never()).save(any(Recinto.class));
    }

    @Test
    void registrarIngreso_CuandoHayEspacio_DebeIncrementarOcupacionYGuardar() {
        // Arrange
        Long id = 1L;
        Recinto r = Recinto.builder().id(id).capacidadMaxima(100).ocupacionActual(40).build();
        when(repo.findById(id)).thenReturn(Optional.of(r));
        when(repo.save(r)).thenReturn(r);

        // Act
        Recinto resultado = service.registrarIngreso(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(41, resultado.getOcupacionActual());
        verify(repo, times(1)).save(r);
    }

    // METODO: registrarEgreso(Long id)
    @Test
    void registrarEgreso_CuandoOcupacionEsCero_DebeLanzarRuntimeException() {
        // Arrange
        Long id = 1L;
        Recinto r = Recinto.builder().id(id).ocupacionActual(0).build();
        when(repo.findById(id)).thenReturn(Optional.of(r));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            service.registrarEgreso(id);
        });
        verify(repo, never()).save(any(Recinto.class));
    }

    @Test
    void registrarEgreso_CuandoOcupacionMayorACero_DebeDecrementarOcupacionYGuardar() {
        // Arrange
        Long id = 1L;
        Recinto r = Recinto.builder().id(id).ocupacionActual(40).build();
        when(repo.findById(id)).thenReturn(Optional.of(r));
        when(repo.save(r)).thenReturn(r);

        // Act
        Recinto resultado = service.registrarEgreso(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(39, resultado.getOcupacionActual());
        verify(repo, times(1)).save(r);
    }
}
