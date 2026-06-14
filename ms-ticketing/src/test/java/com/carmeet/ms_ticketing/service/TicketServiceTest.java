package com.carmeet.ms_ticketing.service;

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

import com.carmeet.ms_ticketing.client.EventoCoreClient;
import com.carmeet.ms_ticketing.client.NotificacionLogClient;
import com.carmeet.ms_ticketing.client.PagoMockClient;
import com.carmeet.ms_ticketing.model.Beneficio;
import com.carmeet.ms_ticketing.model.Ticket;
import com.carmeet.ms_ticketing.repository.TicketRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository repo;

    @Mock
    private EventoCoreClient eventoClient;

    @Mock
    private PagoMockClient pagoClient;

    @Mock
    private NotificacionLogClient notificacionClient;

    @InjectMocks
    private TicketService service;

    // METODO: listar()
    @Test
    void listar_DebeRetornarTodosLosTickets() {
        // Arrange
        Ticket t1 = Ticket.builder().id(1L).categoria("VIP").build();
        Ticket t2 = Ticket.builder().id(2L).categoria("General").build();
        List<Ticket> ticketsEsperados = Arrays.asList(t1, t2);
        
        when(repo.findAll()).thenReturn(ticketsEsperados);

        // Act
        List<Ticket> resultado = service.listar();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(ticketsEsperados, resultado);
        verify(repo, times(1)).findAll();
    }

    // METODO: obtenerPorId(Long id)
    @Test
    void obtenerPorId_CuandoTicketExiste_DebeRetornarTicket() {
        // Arrange
        Long ticketId = 1L;
        Ticket ticketEsperado = Ticket.builder().id(ticketId).categoria("VIP").build();
        
        when(repo.findById(ticketId)).thenReturn(Optional.of(ticketEsperado));

        // Act
        Ticket resultado = service.obtenerPorId(ticketId);

        // Assert
        assertNotNull(resultado);
        assertEquals(ticketId, resultado.getId());
        assertEquals("VIP", resultado.getCategoria());
        verify(repo, times(1)).findById(ticketId);
    }

    @Test
    void obtenerPorId_CuandoTicketNoExiste_DebeLanzarEntityNotFoundException() {
        // Arrange
        Long ticketId = 99L;
        
        when(repo.findById(ticketId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            service.obtenerPorId(ticketId);
        });

        assertEquals("Ticket no encontrado con id: " + ticketId, exception.getMessage());
        verify(repo, times(1)).findById(ticketId);
    }

    // METODO: guardar(Ticket ticket, String bearerToken)
    @Test
    void guardar_CuandoBeneficiosNoEsNulo_DebeAsociarBeneficiosDefinirEstadoPendienteYGuardar() {
        // Arrange
        String token = "Bearer sample_token";
        Beneficio b1 = Beneficio.builder().nombre("Pase VIP").build();
        List<Beneficio> beneficios = new ArrayList<>();
        beneficios.add(b1);

        Ticket ticketAGuardar = Ticket.builder()
                .eventoId(10L)
                .precio(150.0)
                .beneficios(beneficios)
                .build();

        Ticket ticketGuardado = Ticket.builder()
                .id(1L)
                .eventoId(10L)
                .precio(150.0)
                .estado("PENDIENTE")
                .beneficios(beneficios)
                .build();

        doNothing().when(eventoClient).validarEvento(10L, token);
        when(repo.save(any(Ticket.class))).thenReturn(ticketGuardado);

        // Act
        Ticket resultado = service.guardar(ticketAGuardar, token);

        // Assert
        assertNotNull(resultado);
        assertEquals("PENDIENTE", ticketAGuardar.getEstado());
        assertEquals(ticketAGuardar, b1.getTicket());
        assertEquals(1L, resultado.getId());
        verify(eventoClient, times(1)).validarEvento(10L, token);
        verify(repo, times(1)).save(ticketAGuardar);
    }

    @Test
    void guardar_CuandoBeneficiosEsNulo_DebeDefinirEstadoPendienteYGuardar() {
        // Arrange
        String token = "Bearer sample_token";
        Ticket ticketAGuardar = Ticket.builder()
                .eventoId(10L)
                .precio(100.0)
                .beneficios(null)
                .build();

        Ticket ticketGuardado = Ticket.builder()
                .id(2L)
                .eventoId(10L)
                .precio(100.0)
                .estado("PENDIENTE")
                .beneficios(null)
                .build();

        doNothing().when(eventoClient).validarEvento(10L, token);
        when(repo.save(any(Ticket.class))).thenReturn(ticketGuardado);

        // Act
        Ticket resultado = service.guardar(ticketAGuardar, token);

        // Assert
        assertNotNull(resultado);
        assertEquals("PENDIENTE", ticketAGuardar.getEstado());
        assertEquals(2L, resultado.getId());
        verify(eventoClient, times(1)).validarEvento(10L, token);
        verify(repo, times(1)).save(ticketAGuardar);
    }

    // METODO: actualizar(Long id, Ticket datosNuevos)
    @Test
    void actualizar_CuandoTicketExisteYBeneficiosNoEsNulo_DebeActualizarCamposLimpiarYAsociarNuevosBeneficiosYGuardar() {
        // Arrange
        Long ticketId = 1L;
        Beneficio bAntiguo = Beneficio.builder().nombre("Antiguo Beneficio").build();
        List<Beneficio> listaAntigua = new ArrayList<>();
        listaAntigua.add(bAntiguo);

        Ticket ticketExistente = Ticket.builder()
                .id(ticketId)
                .eventoId(5L)
                .precio(50.0)
                .categoria("General")
                .estado("PENDIENTE")
                .username("antiguoUser")
                .beneficios(listaAntigua)
                .build();
        bAntiguo.setTicket(ticketExistente);

        Beneficio bNuevo = Beneficio.builder().nombre("Nuevo Beneficio").build();
        List<Beneficio> listaNueva = new ArrayList<>();
        listaNueva.add(bNuevo);

        Ticket datosNuevos = Ticket.builder()
                .eventoId(6L)
                .precio(75.0)
                .categoria("VIP")
                .estado("CONFIRMADO")
                .username("nuevoUser")
                .beneficios(listaNueva)
                .build();

        when(repo.findById(ticketId)).thenReturn(Optional.of(ticketExistente));
        when(repo.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Ticket resultado = service.actualizar(ticketId, datosNuevos);

        // Assert
        assertNotNull(resultado);
        assertEquals(6L, resultado.getEventoId());
        assertEquals(75.0, resultado.getPrecio());
        assertEquals("VIP", resultado.getCategoria());
        assertEquals("CONFIRMADO", resultado.getEstado());
        assertEquals("nuevoUser", resultado.getUsername());
        assertEquals(1, resultado.getBeneficios().size());
        assertEquals(bNuevo, resultado.getBeneficios().get(0));
        assertEquals(ticketExistente, bNuevo.getTicket());
        verify(repo, times(1)).findById(ticketId);
        verify(repo, times(1)).save(ticketExistente);
    }

    @Test
    void actualizar_CuandoTicketExisteYBeneficiosEsNulo_DebeActualizarCamposLimpiarBeneficiosYGuardar() {
        // Arrange
        Long ticketId = 1L;
        Beneficio bAntiguo = Beneficio.builder().nombre("Antiguo Beneficio").build();
        List<Beneficio> listaAntigua = new ArrayList<>();
        listaAntigua.add(bAntiguo);

        Ticket ticketExistente = Ticket.builder()
                .id(ticketId)
                .eventoId(5L)
                .precio(50.0)
                .categoria("General")
                .estado("PENDIENTE")
                .username("antiguoUser")
                .beneficios(listaAntigua)
                .build();
        bAntiguo.setTicket(ticketExistente);

        Ticket datosNuevos = Ticket.builder()
                .eventoId(6L)
                .precio(75.0)
                .categoria("VIP")
                .estado("CONFIRMADO")
                .username("nuevoUser")
                .beneficios(null)
                .build();

        when(repo.findById(ticketId)).thenReturn(Optional.of(ticketExistente));
        when(repo.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Ticket resultado = service.actualizar(ticketId, datosNuevos);

        // Assert
        assertNotNull(resultado);
        assertEquals(6L, resultado.getEventoId());
        assertEquals(75.0, resultado.getPrecio());
        assertEquals("VIP", resultado.getCategoria());
        assertEquals("CONFIRMADO", resultado.getEstado());
        assertEquals("nuevoUser", resultado.getUsername());
        assertTrue(resultado.getBeneficios().isEmpty());
        verify(repo, times(1)).findById(ticketId);
        verify(repo, times(1)).save(ticketExistente);
    }

    @Test
    void actualizar_CuandoTicketNoExiste_DebeLanzarEntityNotFoundException() {
        // Arrange
        Long ticketId = 99L;
        Ticket datosNuevos = Ticket.builder().eventoId(6L).build();

        when(repo.findById(ticketId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            service.actualizar(ticketId, datosNuevos);
        });

        verify(repo, times(1)).findById(ticketId);
        verify(repo, never()).save(any(Ticket.class));
    }

    // METODO: eliminar(Long id)
    @Test
    void eliminar_CuandoTicketExiste_DebeEliminarCorrectamente() {
        // Arrange
        Long ticketId = 1L;
        when(repo.existsById(ticketId)).thenReturn(true);
        doNothing().when(repo).deleteById(ticketId);

        // Act
        service.eliminar(ticketId);

        // Assert
        verify(repo, times(1)).existsById(ticketId);
        verify(repo, times(1)).deleteById(ticketId);
    }

    @Test
    void eliminar_CuandoTicketNoExiste_DebeLanzarEntityNotFoundException() {
        // Arrange
        Long ticketId = 99L;
        when(repo.existsById(ticketId)).thenReturn(false);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            service.eliminar(ticketId);
        });

        assertEquals("Ticket no encontrado con id: " + ticketId, exception.getMessage());
        verify(repo, times(1)).existsById(ticketId);
        verify(repo, never()).deleteById(anyLong());
    }

    // METODO: obtenerPorEventoId(Long eventoId)
    @Test
    void obtenerPorEventoId_DebeRetornarTicketsAsociadosAlEvento() {
        // Arrange
        Long eventoId = 10L;
        Ticket t1 = Ticket.builder().id(1L).eventoId(eventoId).build();
        List<Ticket> ticketsEsperados = List.of(t1);

        when(repo.findByEventoId(eventoId)).thenReturn(ticketsEsperados);

        // Act
        List<Ticket> resultado = service.obtenerPorEventoId(eventoId);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(ticketsEsperados, resultado);
        verify(repo, times(1)).findByEventoId(eventoId);
    }

    // METODO: obtenerPorUsername(String username)
    @Test
    void obtenerPorUsername_DebeRetornarTicketsAsociadosAlUsuario() {
        // Arrange
        String username = "john_doe";
        Ticket t1 = Ticket.builder().id(1L).username(username).build();
        List<Ticket> ticketsEsperados = List.of(t1);

        when(repo.findByUsername(username)).thenReturn(ticketsEsperados);

        // Act
        List<Ticket> resultado = service.obtenerPorUsername(username);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(ticketsEsperados, resultado);
        verify(repo, times(1)).findByUsername(username);
    }

    // METODO: cancelar(Long id)
    @Test
    void cancelar_CuandoTicketEstaPendiente_DebeCambiarAEstadoCanceladoYGuardar() {
        // Arrange
        Long ticketId = 1L;
        Ticket ticketExistente = Ticket.builder()
                .id(ticketId)
                .estado("PENDIENTE")
                .build();

        when(repo.findById(ticketId)).thenReturn(Optional.of(ticketExistente));
        when(repo.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Ticket resultado = service.cancelar(ticketId);

        // Assert
        assertNotNull(resultado);
        assertEquals("CANCELADO", resultado.getEstado());
        verify(repo, times(1)).findById(ticketId);
        verify(repo, times(1)).save(ticketExistente);
    }

    @Test
    void cancelar_CuandoTicketNoEstaPendiente_DebeLanzarRuntimeException() {
        // Arrange
        Long ticketId = 1L;
        Ticket ticketExistente = Ticket.builder()
                .id(ticketId)
                .estado("PAGADO")
                .build();

        when(repo.findById(ticketId)).thenReturn(Optional.of(ticketExistente));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.cancelar(ticketId);
        });

        assertTrue(exception.getMessage().contains("Solo se pueden cancelar tickets en estado PENDIENTE"));
        verify(repo, times(1)).findById(ticketId);
        verify(repo, never()).save(any(Ticket.class));
    }

    // METODO: pagar(Long id, String metodoPago, String bearerToken)
    @Test
    void pagar_CuandoTicketEstaPendienteYElUsuarioTieneUsername_DebeProcesarPagoCambiarEstadoAPagadoEnviarNotificacionYGuardar() {
        // Arrange
        Long ticketId = 1L;
        String metodoPago = "CREDITO";
        String token = "Bearer user_token";
        
        Ticket ticketExistente = Ticket.builder()
                .id(ticketId)
                .precio(100.0)
                .estado("PENDIENTE")
                .username("john_doe")
                .build();

        when(repo.findById(ticketId)).thenReturn(Optional.of(ticketExistente));
        when(pagoClient.procesarPago(ticketId, 100.0, metodoPago, token)).thenReturn(new java.util.HashMap<>());
        when(repo.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(notificacionClient).enviar(
                eq("john_doe"),
                contains("ha sido PAGADO"),
                eq(token)
        );

        // Act
        Ticket resultado = service.pagar(ticketId, metodoPago, token);

        // Assert
        assertNotNull(resultado);
        assertEquals("PAGADO", resultado.getEstado());
        verify(repo, times(1)).findById(ticketId);
        verify(pagoClient, times(1)).procesarPago(ticketId, 100.0, metodoPago, token);
        verify(repo, times(1)).save(ticketExistente);
        verify(notificacionClient, times(1)).enviar(
                eq("john_doe"),
                contains("ha sido PAGADO"),
                eq(token)
        );
    }

    @Test
    void pagar_CuandoTicketEstaPendienteYElUsuarioNoTieneUsername_DebeProcesarPagoCambiarEstadoAPagadoNoEnviarNotificacionYGuardar() {
        // Arrange
        Long ticketId = 1L;
        String metodoPago = "DEBITO";
        String token = "Bearer user_token";
        
        Ticket ticketExistente = Ticket.builder()
                .id(ticketId)
                .precio(100.0)
                .estado("PENDIENTE")
                .username(null)
                .build();

        when(repo.findById(ticketId)).thenReturn(Optional.of(ticketExistente));
        when(pagoClient.procesarPago(ticketId, 100.0, metodoPago, token)).thenReturn(new java.util.HashMap<>());
        when(repo.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Ticket resultado = service.pagar(ticketId, metodoPago, token);

        // Assert
        assertNotNull(resultado);
        assertEquals("PAGADO", resultado.getEstado());
        verify(repo, times(1)).findById(ticketId);
        verify(pagoClient, times(1)).procesarPago(ticketId, 100.0, metodoPago, token);
        verify(repo, times(1)).save(ticketExistente);
        verify(notificacionClient, never()).enviar(anyString(), anyString(), anyString());
    }

    @Test
    void pagar_CuandoTicketNoEstaPendiente_DebeLanzarRuntimeException() {
        // Arrange
        Long ticketId = 1L;
        String metodoPago = "CREDITO";
        String token = "Bearer user_token";
        
        Ticket ticketExistente = Ticket.builder()
                .id(ticketId)
                .estado("CANCELADO")
                .build();

        when(repo.findById(ticketId)).thenReturn(Optional.of(ticketExistente));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.pagar(ticketId, metodoPago, token);
        });

        assertTrue(exception.getMessage().contains("Solo se pueden pagar tickets en estado PENDIENTE"));
        verify(repo, times(1)).findById(ticketId);
        verify(pagoClient, never()).procesarPago(anyLong(), anyDouble(), anyString(), anyString());
        verify(repo, never()).save(any(Ticket.class));
        verify(notificacionClient, never()).enviar(anyString(), anyString(), anyString());
    }

    @Test
    void pagar_CuandoProcesarPagoFalla_DebeLanzarRuntimeExceptionYNoGuardar() {
        // Arrange
        Long ticketId = 1L;
        String metodoPago = "CREDITO";
        String token = "Bearer user_token";
        
        Ticket ticketExistente = Ticket.builder()
                .id(ticketId)
                .precio(100.0)
                .estado("PENDIENTE")
                .username("john_doe")
                .build();

        when(repo.findById(ticketId)).thenReturn(Optional.of(ticketExistente));
        doThrow(new RuntimeException("Error en pasarela")).when(pagoClient)
                .procesarPago(ticketId, 100.0, metodoPago, token);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.pagar(ticketId, metodoPago, token);
        });

        assertTrue(exception.getMessage().contains("Pago rechazado: Error en pasarela"));
        verify(repo, times(1)).findById(ticketId);
        verify(pagoClient, times(1)).procesarPago(ticketId, 100.0, metodoPago, token);
        verify(repo, never()).save(any(Ticket.class));
        verify(notificacionClient, never()).enviar(anyString(), anyString(), anyString());
    }
}
