package com.carmeet.ms_vehicle_registry.service;

import com.carmeet.ms_vehicle_registry.model.Vehiculo;
import com.carmeet.ms_vehicle_registry.model.Mantenimiento;
import com.carmeet.ms_vehicle_registry.repository.VehiculoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehiculoService {

    private final VehiculoRepository repo;

    public List<Vehiculo> listar() {
        return repo.findAll();
    }

    public Vehiculo obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Vehiculo no encontrado con id: " + id));
    }

    public Vehiculo guardar(Vehiculo vehiculo) {
        if (vehiculo.getMantenimientos() != null) {
            vehiculo.getMantenimientos().forEach(m -> m.setVehiculo(vehiculo));
        }
        return repo.save(vehiculo);
    }

    public Vehiculo actualizar(Long id, Vehiculo datosNuevos) {
        Vehiculo existente = obtenerPorId(id);
        existente.setMarca(datosNuevos.getMarca());
        existente.setModelo(datosNuevos.getModelo());
        existente.setAnio(datosNuevos.getAnio());

        existente.getMantenimientos().clear();
        if (datosNuevos.getMantenimientos() != null) {
            datosNuevos.getMantenimientos().forEach(m -> {
                m.setVehiculo(existente);
                existente.getMantenimientos().add(m);
            });
        }

        return repo.save(existente);
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Vehiculo no encontrado con id: " + id);
        }
        repo.deleteById(id);
    }

    
    public List<Mantenimiento> listarMantenimientos(Long vehiculoId) {
        return obtenerPorId(vehiculoId).getMantenimientos();
    }

    
    public Vehiculo agregarMantenimiento(Long vehiculoId, Mantenimiento mantenimiento) {
        Vehiculo vehiculo = obtenerPorId(vehiculoId);
        mantenimiento.setVehiculo(vehiculo);
        vehiculo.getMantenimientos().add(mantenimiento);
        return repo.save(vehiculo);
    }

    
    public List<Vehiculo> buscarPorModelo(String modelo) {
        if (modelo == null || modelo.isBlank()) {
            throw new RuntimeException("El parámetro de búsqueda no puede estar vacío");
        }
        return repo.findByModeloContainingIgnoreCase(modelo);
    }
}
