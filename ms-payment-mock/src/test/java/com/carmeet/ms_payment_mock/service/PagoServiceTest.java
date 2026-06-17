package com.carmeet.ms_payment_mock.service;

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

import com.carmeet.ms_payment_mock.model.Pago;
import com.carmeet.ms_payment_mock.model.TransaccionLog;
import com.carmeet.ms_payment_mock.repository.PagoRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class PagoServiceTest {

    @Mock
    private PagoRepository repo;

    @InjectMocks
    private PagoService service;

    // METODO: listar()
    @Test
    void listar_DebeRetornarTodosLosPagos() {
        // Arrange
        Pago p1 = Pago.builder().id(1L).monto(10.0).build();
        Pago p2 = Pago.builder().id(2L).monto(20.0).build();
        List<Pago> list = Arrays.asList(p1, p2);

        when(repo.findAll()).thenReturn(list);

        // Act
        List<Pago> resultado = service.listar();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(repo, times(1)).findAll();
    }

    // METODO: obtenerPorId(Long id)
    @Test
    void obtenerPorId_CuandoExiste_DebeRetornarPago() {
        // Arrange
        Long id = 1L;
        Pago p = Pago.builder().id(id).monto(100.0).build();
        when(repo.findById(id)).thenReturn(Optional.of(p));

        // Act
        Pago resultado = service.obtenerPorId(id);

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

    // METODO: guardar(Pago pago)
    @Test
    void guardar_CuandoLogsNoEsNulo_DebeAsociarLogsYGuardar() {
        // Arrange
        TransaccionLog l = TransaccionLog.builder().estado("APROBADO").build();
        List<TransaccionLog> logs = new ArrayList<>();
        logs.add(l);

        Pago p = Pago.builder().monto(50.0).logs(logs).build();
        Pago guardado = Pago.builder().id(1L).monto(50.0).logs(logs).build();

        when(repo.save(p)).thenReturn(guardado);

        // Act
        Pago resultado = service.guardar(p);

        // Assert
        assertNotNull(resultado);
        assertEquals(p, l.getPago());
        verify(repo, times(1)).save(p);
    }

    @Test
    void guardar_CuandoLogsEsNulo_DebeGuardarSinModificarLogs() {
        Pago p = Pago.builder().monto(50.0).logs(null).build();
        Pago guardado = Pago.builder().id(1L).monto(50.0).logs(null).build();

        when(repo.save(p)).thenReturn(guardado);

        Pago resultado = service.guardar(p);

        assertNotNull(resultado);
        verify(repo, times(1)).save(p);
    }

    // METODO: actualizar(Long id, Pago datosNuevos)
    @Test
    void actualizar_CuandoExisteYLogsNoEsNulo_DebeActualizarYGuardar() {
        // Arrange
        Long id = 1L;
        TransaccionLog lOld = TransaccionLog.builder().estado("RECHAZADO").build();
        List<TransaccionLog> lListOld = new ArrayList<>();
        lListOld.add(lOld);

        Pago existente = Pago.builder()
                .id(id)
                .ticketId(5L)
                .monto(100.0)
                .metodoPago("EFECTIVO")
                .logs(lListOld)
                .build();
        lOld.setPago(existente);

        TransaccionLog lNew = TransaccionLog.builder().estado("APROBADO").build();
        List<TransaccionLog> lListNew = new ArrayList<>();
        lListNew.add(lNew);

        Pago datosNuevos = Pago.builder()
                .ticketId(6L)
                .monto(150.0)
                .metodoPago("TARJETA")
                .logs(lListNew)
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(existente));
        when(repo.save(existente)).thenReturn(existente);

        // Act
        Pago resultado = service.actualizar(id, datosNuevos);

        // Assert
        assertNotNull(resultado);
        assertEquals(6L, resultado.getTicketId());
        assertEquals(150.0, resultado.getMonto());
        assertEquals("TARJETA", resultado.getMetodoPago());
        assertEquals(1, resultado.getLogs().size());
        assertEquals(lNew, resultado.getLogs().get(0));
        assertEquals(existente, lNew.getPago());
    }

    @Test
    void actualizar_CuandoExisteYLogsEsNulo_DebeActualizarYGuardarSinNuevosLogs() {
        Long id = 1L;
        Pago existente = Pago.builder().id(id).logs(new ArrayList<>()).build();
        Pago datosNuevos = Pago.builder().ticketId(6L).logs(null).build();

        when(repo.findById(id)).thenReturn(Optional.of(existente));
        when(repo.save(existente)).thenReturn(existente);

        Pago resultado = service.actualizar(id, datosNuevos);

        assertNotNull(resultado);
        assertEquals(6L, resultado.getTicketId());
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

    // METODO: obtenerPorTicketId(Long ticketId)
    @Test
    void obtenerPorTicketId_CuandoExiste_DebeRetornarPago() {
        // Arrange
        Long tId = 5L;
        Pago p = Pago.builder().ticketId(tId).build();
        when(repo.findByTicketId(tId)).thenReturn(Optional.of(p));

        // Act
        Pago resultado = service.obtenerPorTicketId(tId);

        // Assert
        assertNotNull(resultado);
        assertEquals(tId, resultado.getTicketId());
    }

    @Test
    void obtenerPorTicketId_CuandoNoExiste_DebeLanzarEntityNotFoundException() {
        // Arrange
        Long tId = 99L;
        when(repo.findByTicketId(tId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            service.obtenerPorTicketId(tId);
        });
    }

    // METODO: procesarPago(Pago pago)
    @Test
    void procesarPago_CuandoMontoEsInvalido_DebeLanzarRuntimeException() {
        // Arrange
        Pago pNull = Pago.builder().monto(null).build();
        Pago pZero = Pago.builder().monto(0.0).build();
        Pago pNeg = Pago.builder().monto(-5.0).build();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.procesarPago(pNull));
        assertThrows(RuntimeException.class, () -> service.procesarPago(pZero));
        assertThrows(RuntimeException.class, () -> service.procesarPago(pNeg));
    }

    @Test
    void procesarPago_DebeAgregarLogDeTransaccionYGuardar() {
        // Arrange
        when(repo.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean hitApproved = false;
        boolean hitRejected = false;

        // Loop a few times to probabilistically hit both random branches
        for (int i = 0; i < 50; i++) {
            Pago p = Pago.builder().monto(100.0).logs(new ArrayList<>()).build();
            try {
                Pago resultado = service.procesarPago(p);
                assertNotNull(resultado);
                assertEquals(1, resultado.getLogs().size());
                assertEquals("APROBADO", resultado.getLogs().get(0).getEstado());
                hitApproved = true;
            } catch (RuntimeException e) {
                assertTrue(e.getMessage().contains("Pago RECHAZADO"));
                assertEquals(1, p.getLogs().size());
                assertEquals("RECHAZADO", p.getLogs().get(0).getEstado());
                hitRejected = true;
            }
            if (hitApproved && hitRejected) {
                break;
            }
        }
        assertTrue(hitApproved && hitRejected, "Deberia haber probado ambos casos probabilísticamente");
        verify(repo, atLeastOnce()).save(any(Pago.class));
    }

    // METODO: obtenerLogs(Long pagoId)
    @Test
    void obtenerLogs_DebeRetornarListaDeLogs() {
        // Arrange
        Long pagoId = 1L;
        TransaccionLog log = new TransaccionLog();
        List<TransaccionLog> logs = List.of(log);
        Pago p = Pago.builder().id(pagoId).logs(logs).build();

        when(repo.findById(pagoId)).thenReturn(Optional.of(p));

        // Act
        List<TransaccionLog> resultado = service.obtenerLogs(pagoId);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(logs, resultado);
    }
}
