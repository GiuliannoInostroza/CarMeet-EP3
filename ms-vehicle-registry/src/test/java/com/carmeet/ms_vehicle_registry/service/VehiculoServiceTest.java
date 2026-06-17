package com.carmeet.ms_vehicle_registry.service;

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

import com.carmeet.ms_vehicle_registry.model.Vehiculo;
import com.carmeet.ms_vehicle_registry.model.Mantenimiento;
import com.carmeet.ms_vehicle_registry.repository.VehiculoRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class VehiculoServiceTest {

    @Mock
    private VehiculoRepository repo;

    @InjectMocks
    private VehiculoService service;

    // METODO: listar()
    @Test
    void listar_DebeRetornarTodosLosVehiculos() {
        // Arrange
        Vehiculo v1 = Vehiculo.builder().id(1L).marca("Toyota").build();
        Vehiculo v2 = Vehiculo.builder().id(2L).marca("Nissan").build();
        List<Vehiculo> list = Arrays.asList(v1, v2);

        when(repo.findAll()).thenReturn(list);

        // Act
        List<Vehiculo> resultado = service.listar();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(repo, times(1)).findAll();
    }

    // METODO: obtenerPorId(Long id)
    @Test
    void obtenerPorId_CuandoExiste_DebeRetornarVehiculo() {
        // Arrange
        Long id = 1L;
        Vehiculo v = Vehiculo.builder().id(id).marca("Toyota").build();
        when(repo.findById(id)).thenReturn(Optional.of(v));

        // Act
        Vehiculo resultado = service.obtenerPorId(id);

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

    // METODO: guardar(Vehiculo vehiculo)
    @Test
    void guardar_CuandoMantenimientosNoEsNulo_DebeAsociarMantenimientosYGuardar() {
        // Arrange
        Mantenimiento m = Mantenimiento.builder().descripcion("Cambio Aceite").build();
        List<Mantenimiento> mantList = new ArrayList<>();
        mantList.add(m);

        Vehiculo v = Vehiculo.builder().marca("Toyota").mantenimientos(mantList).build();
        Vehiculo guardado = Vehiculo.builder().id(1L).marca("Toyota").mantenimientos(mantList).build();

        when(repo.save(v)).thenReturn(guardado);

        // Act
        Vehiculo resultado = service.guardar(v);

        // Assert
        assertNotNull(resultado);
        assertEquals(v, m.getVehiculo());
        verify(repo, times(1)).save(v);
    }

    @Test
    void guardar_CuandoMantenimientosEsNulo_DebeGuardarSinModificarMantenimientos() {
        Vehiculo v = Vehiculo.builder().marca("Toyota").mantenimientos(null).build();
        Vehiculo guardado = Vehiculo.builder().id(1L).marca("Toyota").mantenimientos(null).build();

        when(repo.save(v)).thenReturn(guardado);

        Vehiculo resultado = service.guardar(v);

        assertNotNull(resultado);
        verify(repo, times(1)).save(v);
    }

    // METODO: actualizar(Long id, Vehiculo datosNuevos)
    @Test
    void actualizar_CuandoExisteYMantenimientosNoEsNulo_DebeActualizarYGuardar() {
        // Arrange
        Long id = 1L;
        Mantenimiento mOld = Mantenimiento.builder().descripcion("Old oil").build();
        List<Mantenimiento> lOld = new ArrayList<>();
        lOld.add(mOld);

        Vehiculo existente = Vehiculo.builder()
                .id(id)
                .marca("Nissan")
                .modelo("370Z")
                .anio(2018)
                .mantenimientos(lOld)
                .build();
        mOld.setVehiculo(existente);

        Mantenimiento mNew = Mantenimiento.builder().descripcion("New brakes").build();
        List<Mantenimiento> lNew = new ArrayList<>();
        lNew.add(mNew);

        Vehiculo datosNuevos = Vehiculo.builder()
                .marca("Toyota")
                .modelo("Supra")
                .anio(2021)
                .mantenimientos(lNew)
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(existente));
        when(repo.save(existente)).thenReturn(existente);

        // Act
        Vehiculo resultado = service.actualizar(id, datosNuevos);

        // Assert
        assertNotNull(resultado);
        assertEquals("Toyota", resultado.getMarca());
        assertEquals("Supra", resultado.getModelo());
        assertEquals(2021, resultado.getAnio());
        assertEquals(1, resultado.getMantenimientos().size());
        assertEquals(mNew, resultado.getMantenimientos().get(0));
        assertEquals(existente, mNew.getVehiculo());
    }

    @Test
    void actualizar_CuandoExisteYMantenimientosEsNulo_DebeActualizarYGuardarSinNuevosMantenimientos() {
        Long id = 1L;
        Vehiculo existente = Vehiculo.builder().id(id).mantenimientos(new ArrayList<>()).build();
        Vehiculo datosNuevos = Vehiculo.builder().marca("Toyota").mantenimientos(null).build();

        when(repo.findById(id)).thenReturn(Optional.of(existente));
        when(repo.save(existente)).thenReturn(existente);

        Vehiculo resultado = service.actualizar(id, datosNuevos);

        assertNotNull(resultado);
        assertEquals("Toyota", resultado.getMarca());
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

    // METODO: listarMantenimientos(Long vehiculoId)
    @Test
    void listarMantenimientos_DebeRetornarListaDeMantenimientos() {
        // Arrange
        Long vId = 1L;
        Mantenimiento m = new Mantenimiento();
        List<Mantenimiento> list = List.of(m);
        Vehiculo v = Vehiculo.builder().id(vId).mantenimientos(list).build();

        when(repo.findById(vId)).thenReturn(Optional.of(v));

        // Act
        List<Mantenimiento> resultado = service.listarMantenimientos(vId);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(list, resultado);
    }

    // METODO: agregarMantenimiento(Long vehiculoId, Mantenimiento mantenimiento)
    @Test
    void agregarMantenimiento_DebeAsociarMantenimientoYGuardarVehiculo() {
        // Arrange
        Long vId = 1L;
        Mantenimiento m = Mantenimiento.builder().descripcion("Frenos").build();
        Vehiculo v = Vehiculo.builder().id(vId).mantenimientos(new ArrayList<>()).build();

        when(repo.findById(vId)).thenReturn(Optional.of(v));
        when(repo.save(v)).thenReturn(v);

        // Act
        Vehiculo resultado = service.agregarMantenimiento(vId, m);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getMantenimientos().size());
        assertEquals(m, resultado.getMantenimientos().get(0));
        assertEquals(v, m.getVehiculo());
        verify(repo, times(1)).save(v);
    }

    // METODO: buscarPorModelo(String modelo)
    @Test
    void buscarPorModelo_CuandoModeloEsInvalido_DebeLanzarRuntimeException() {
        assertThrows(RuntimeException.class, () -> service.buscarPorModelo(null));
        assertThrows(RuntimeException.class, () -> service.buscarPorModelo("   "));
    }

    @Test
    void buscarPorModelo_CuandoModeloEsValido_DebeRetornarVehiculos() {
        // Arrange
        String q = "GTR";
        List<Vehiculo> list = List.of(new Vehiculo());
        when(repo.findByModeloContainingIgnoreCase(q)).thenReturn(list);

        // Act
        List<Vehiculo> resultado = service.buscarPorModelo(q);

        // Assert
        assertEquals(1, resultado.size());
    }
}
