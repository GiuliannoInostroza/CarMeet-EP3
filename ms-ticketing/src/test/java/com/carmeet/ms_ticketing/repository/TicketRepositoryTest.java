package com.carmeet.ms_ticketing.repository;

import com.carmeet.ms_ticketing.model.Ticket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TicketRepositoryTest {

    @Autowired
    private TicketRepository repository;

    @Test
    void debeGuardarTicket() {
        Ticket t = Ticket.builder()
                .eventoId(1L)
                .precio(150.0)
                .categoria("VIP")
                .estado("PENDIENTE")
                .username("john_doe")
                .build();

        Ticket guardado = repository.save(t);
        assertNotNull(guardado.getId());
        assertEquals("VIP", guardado.getCategoria());
        assertEquals(150.0, guardado.getPrecio());
    }

    @Test
    void debeBuscarTicketPorId() {
        Ticket t = Ticket.builder()
                .eventoId(2L)
                .precio(80.0)
                .categoria("General")
                .estado("PAGADO")
                .username("maria")
                .build();

        Ticket guardado = repository.save(t);
        Optional<Ticket> resultado = repository.findById(guardado.getId());
        assertTrue(resultado.isPresent());
        assertEquals("General", resultado.get().getCategoria());
    }

    @Test
    void debeListarTickets() {
        repository.save(Ticket.builder().eventoId(10L).precio(50.0).build());
        repository.save(Ticket.builder().eventoId(20L).precio(60.0).build());

        List<Ticket> resultado = repository.findAll();
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.size() >= 2);
    }

    @Test
    void debeEliminarTicket() {
        Ticket t = Ticket.builder().eventoId(30L).precio(100.0).build();
        Ticket guardado = repository.save(t);

        repository.deleteById(guardado.getId());
        Optional<Ticket> resultado = repository.findById(guardado.getId());
        assertFalse(resultado.isPresent());
    }

    @Test
    void debeBuscarPorEventoId() {
        repository.save(Ticket.builder().eventoId(100L).precio(10.0).build());
        repository.save(Ticket.builder().eventoId(100L).precio(20.0).build());
        repository.save(Ticket.builder().eventoId(200L).precio(30.0).build());

        List<Ticket> resultados = repository.findByEventoId(100L);
        assertEquals(2, resultados.size());
    }

    @Test
    void debeBuscarPorUsername() {
        repository.save(Ticket.builder().eventoId(1L).username("testuser").build());
        repository.save(Ticket.builder().eventoId(2L).username("other").build());

        List<Ticket> resultados = repository.findByUsername("testuser");
        assertEquals(1, resultados.size());
        assertEquals("testuser", resultados.get(0).getUsername());
    }
}
