package com.carmeet.ms_venue_capacity.repository;

import com.carmeet.ms_venue_capacity.model.Recinto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RecintoRepositoryTest {

    @Autowired
    private RecintoRepository repository;

    @Test
    void debeGuardarRecinto() {
        Recinto r = Recinto.builder()
                .nombre("Autodromo Principal")
                .capacidadMaxima(5000)
                .ocupacionActual(0)
                .build();

        Recinto guardado = repository.save(r);
        assertNotNull(guardado.getId());
        assertEquals("Autodromo Principal", guardado.getNombre());
        assertEquals(5000, guardado.getCapacidadMaxima());
    }

    @Test
    void debeBuscarRecintoPorId() {
        Recinto r = Recinto.builder()
                .nombre("Pista de Aceleracion")
                .capacidadMaxima(2000)
                .ocupacionActual(150)
                .build();

        Recinto guardado = repository.save(r);
        Optional<Recinto> resultado = repository.findById(guardado.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Pista de Aceleracion", resultado.get().getNombre());
    }

    @Test
    void debeListarRecintos() {
        repository.save(Recinto.builder().nombre("R1").capacidadMaxima(100).ocupacionActual(0).build());
        repository.save(Recinto.builder().nombre("R2").capacidadMaxima(200).ocupacionActual(0).build());

        List<Recinto> resultado = repository.findAll();
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.size() >= 2);
    }

    @Test
    void debeEliminarRecinto() {
        Recinto r = Recinto.builder().nombre("R3").capacidadMaxima(300).ocupacionActual(0).build();
        Recinto guardado = repository.save(r);

        repository.deleteById(guardado.getId());
        Optional<Recinto> resultado = repository.findById(guardado.getId());
        assertFalse(resultado.isPresent());
    }
}
