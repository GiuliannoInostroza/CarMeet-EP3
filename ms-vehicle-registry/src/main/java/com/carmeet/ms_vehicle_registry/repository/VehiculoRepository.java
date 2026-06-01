package com.carmeet.ms_vehicle_registry.repository;

import com.carmeet.ms_vehicle_registry.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    List<Vehiculo> findByModeloContainingIgnoreCase(String modelo);
}
