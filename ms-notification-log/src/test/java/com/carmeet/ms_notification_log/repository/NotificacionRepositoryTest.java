package com.carmeet.ms_notification_log.repository;

import com.carmeet.ms_notification_log.model.Notificacion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class NotificacionRepositoryTest {

    @Autowired
    private NotificacionRepository repository;

    @Test
    void debeGuardarNotificacion() {
        Notificacion n = Notificacion.builder()
                .destinatario("carlos@mail.com")
                .mensaje("Tu inscripción ha sido aprobada")
                .leida(false)
                .build();

        Notificacion guardada = repository.save(n);
        assertNotNull(guardada.getId());
        assertEquals("carlos@mail.com", guardada.getDestinatario());
        assertFalse(guardada.getLeida());
    }

    @Test
    void debeBuscarNotificacionPorId() {
        Notificacion n = Notificacion.builder()
                .destinatario("maria@mail.com")
                .mensaje("Nueva puntuación asignada")
                .leida(true)
                .build();

        Notificacion guardada = repository.save(n);
        Optional<Notificacion> resultado = repository.findById(guardada.getId());
        assertTrue(resultado.isPresent());
        assertEquals("maria@mail.com", resultado.get().getDestinatario());
        assertTrue(resultado.get().getLeida());
    }

    @Test
    void debeListarNotificaciones() {
        repository.save(Notificacion.builder().destinatario("user1@mail.com").mensaje("M1").build());
        repository.save(Notificacion.builder().destinatario("user2@mail.com").mensaje("M2").build());

        List<Notificacion> resultado = repository.findAll();
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.size() >= 2);
    }

    @Test
    void debeEliminarNotificacion() {
        Notificacion n = Notificacion.builder().destinatario("user3@mail.com").mensaje("M3").build();
        Notificacion guardada = repository.save(n);

        repository.deleteById(guardada.getId());
        Optional<Notificacion> resultado = repository.findById(guardada.getId());
        assertFalse(resultado.isPresent());
    }

    @Test
    void debeBuscarPorDestinatario() {
        repository.save(Notificacion.builder().destinatario("juan@mail.com").mensaje("M1").build());
        repository.save(Notificacion.builder().destinatario("juan@mail.com").mensaje("M2").build());
        repository.save(Notificacion.builder().destinatario("pedro@mail.com").mensaje("M3").build());

        List<Notificacion> resultados = repository.findByDestinatario("juan@mail.com");
        assertEquals(2, resultados.size());
    }

    @Test
    void debeBuscarPorDestinatarioAndLeida() {
        repository.save(Notificacion.builder().destinatario("juan@mail.com").mensaje("M1").leida(true).build());
        repository.save(Notificacion.builder().destinatario("juan@mail.com").mensaje("M2").leida(false).build());

        List<Notificacion> leidas = repository.findByDestinatarioAndLeida("juan@mail.com", true);
        assertEquals(1, leidas.size());
        assertEquals("M1", leidas.get(0).getMensaje());

        List<Notificacion> noLeidas = repository.findByDestinatarioAndLeida("juan@mail.com", false);
        assertEquals(1, noLeidas.size());
        assertEquals("M2", noLeidas.get(0).getMensaje());
    }
}
