package com.carmeet.ms_vehicle_registry.repository;

import com.carmeet.ms_vehicle_registry.model.Vehiculo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class VehiculoRepositoryTest {

    @Autowired
    private VehiculoRepository repository;

    @Test
    void debeGuardarVehiculo() {
        Vehiculo v = Vehiculo.builder()
                .marca("Toyota")
                .modelo("Supra")
                .anio(2020)
                .build();
        Vehiculo guardado = repository.save(v);
        assertNotNull(guardado.getId());
        assertEquals("Toyota", guardado.getMarca());
    }

    @Test
    void debeBuscarVehiculoPorId() {
        Vehiculo v = Vehiculo.builder()
                .marca("Nissan")
                .modelo("GTR")
                .anio(2018)
                .build();
        Vehiculo guardado = repository.save(v);
        Optional<Vehiculo> resultado = repository.findById(guardado.getId());
        assertTrue(resultado.isPresent());
        assertEquals("GTR", resultado.get().getModelo());
    }

    @Test
    void debeListarVehiculos() {
        repository.save(Vehiculo.builder().marca("Subaru").modelo("WRX").anio(2019).build());
        repository.save(Vehiculo.builder().marca("Mazda").modelo("MX-5").anio(2021).build());
        List<Vehiculo> resultado = repository.findAll();
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.size() >= 2);
    }

    @Test
    void debeEliminarVehiculo() {
        Vehiculo v = Vehiculo.builder()
                .marca("Honda")
                .modelo("Civic")
                .anio(2017)
                .build();
        Vehiculo guardado = repository.save(v);
        repository.deleteById(guardado.getId());
        Optional<Vehiculo> resultado = repository.findById(guardado.getId());
        assertFalse(resultado.isPresent());
    }

    @Test
    void debeBuscarPorModeloConteniendoIgnorarMayusculas() {
        repository.save(Vehiculo.builder().marca("Chevrolet").modelo("Camaro SS").anio(2015).build());
        repository.save(Vehiculo.builder().marca("Ford").modelo("Mustang GT").anio(2016).build());
        
        List<Vehiculo> camaros = repository.findByModeloContainingIgnoreCase("camaro");
        assertEquals(1, camaros.size());
        assertEquals("Camaro SS", camaros.get(0).getModelo());
    }
}
