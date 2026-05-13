package com.carmeet.ms_vehicle_registry.service;

import com.carmeet.ms_vehicle_registry.model.Vehiculo;
import com.carmeet.ms_vehicle_registry.dto.VehiculoDTO;
import com.carmeet.ms_vehicle_registry.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehiculoService {

    private final VehiculoRepository repo;

    public Vehiculo registrar(VehiculoDTO dto) {
        Vehiculo v = Vehiculo.builder()
                .patente(dto.getPatente())
                .marca(dto.getMarca())
                .modelo(dto.getModelo())
                .username(SecurityContextHolder.getContext().getAuthentication().getName()) // Extraído del token
                .build();
        return repo.save(v);
    }
}
