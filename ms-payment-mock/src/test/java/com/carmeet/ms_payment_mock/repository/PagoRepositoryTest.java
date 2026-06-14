package com.carmeet.ms_payment_mock.repository;

import com.carmeet.ms_payment_mock.model.Pago;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PagoRepositoryTest {

    @Autowired
    private PagoRepository repository;

    @Test
    void debeGuardarPago() {
        Pago p = Pago.builder()
                .ticketId(10L)
                .monto(150.0)
                .metodoPago("Tarjeta de Crédito")
                .build();

        Pago guardado = repository.save(p);
        assertNotNull(guardado.getId());
        assertEquals(10L, guardado.getTicketId());
        assertEquals(150.0, guardado.getMonto());
    }

    @Test
    void debeBuscarPagoPorId() {
        Pago p = Pago.builder()
                .ticketId(20L)
                .monto(200.0)
                .metodoPago("PayPal")
                .build();

        Pago guardado = repository.save(p);
        Optional<Pago> resultado = repository.findById(guardado.getId());
        assertTrue(resultado.isPresent());
        assertEquals("PayPal", resultado.get().getMetodoPago());
    }

    @Test
    void debeListarPagos() {
        repository.save(Pago.builder().ticketId(1L).monto(50.0).build());
        repository.save(Pago.builder().ticketId(2L).monto(75.0).build());

        List<Pago> resultado = repository.findAll();
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.size() >= 2);
    }

    @Test
    void debeEliminarPago() {
        Pago p = Pago.builder().ticketId(3L).monto(100.0).build();
        Pago guardado = repository.save(p);

        repository.deleteById(guardado.getId());
        Optional<Pago> resultado = repository.findById(guardado.getId());
        assertFalse(resultado.isPresent());
    }

    @Test
    void debeBuscarPorTicketId() {
        repository.save(Pago.builder().ticketId(100L).monto(80.0).build());
        repository.save(Pago.builder().ticketId(200L).monto(90.0).build());

        Optional<Pago> resultado = repository.findByTicketId(100L);
        assertTrue(resultado.isPresent());
        assertEquals(80.0, resultado.get().getMonto());
    }
}
