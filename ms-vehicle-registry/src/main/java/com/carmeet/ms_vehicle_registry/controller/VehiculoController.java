package com.carmeet.ms_vehicle_registry.controller;

import com.carmeet.ms_vehicle_registry.dto.ApiResponse;
import com.carmeet.ms_vehicle_registry.model.Vehiculo;
import com.carmeet.ms_vehicle_registry.model.Mantenimiento;
import com.carmeet.ms_vehicle_registry.dto.VehiculoDTO;
import com.carmeet.ms_vehicle_registry.dto.MantenimientoDTO;
import com.carmeet.ms_vehicle_registry.service.VehiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VehiculoDTO>>> listar() {
        List<VehiculoDTO> lista = service.listar().stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<VehiculoDTO>>builder().success(true).message("Listado").data(lista).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehiculoDTO>> obtenerPorId(@PathVariable Long id) {
        VehiculoDTO dto = toDTO(service.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.<VehiculoDTO>builder().success(true).message("Encontrado").data(dto).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VehiculoDTO>> guardar(@Valid @RequestBody VehiculoDTO dto) {
        Vehiculo nuevo = service.guardar(toEntity(dto));
        return ResponseEntity.status(201).body(ApiResponse.<VehiculoDTO>builder().success(true).message("Creado").data(toDTO(nuevo)).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehiculoDTO>> actualizar(@PathVariable Long id, @Valid @RequestBody VehiculoDTO dto) {
        Vehiculo actualizado = service.actualizar(id, toEntity(dto));
        return ResponseEntity.ok(ApiResponse.<VehiculoDTO>builder().success(true).message("Actualizado").data(toDTO(actualizado)).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Eliminado").build());
    }

    private VehiculoDTO toDTO(Vehiculo e) {
        VehiculoDTO dto = new VehiculoDTO();
        dto.setId(e.getId());
        dto.setMarca(e.getMarca());
        dto.setModelo(e.getModelo());
        dto.setAnio(e.getAnio());
        if(e.getMantenimientos() != null) {
            dto.setMantenimientos(e.getMantenimientos().stream().map(p -> {
                MantenimientoDTO pdto = new MantenimientoDTO();
                pdto.setId(p.getId());
                pdto.setDescripcion(p.getDescripcion());
                return pdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Vehiculo toEntity(VehiculoDTO dto) {
        Vehiculo e = new Vehiculo();
        e.setMarca(dto.getMarca());
        e.setModelo(dto.getModelo());
        e.setAnio(dto.getAnio());
        if(dto.getMantenimientos() != null) {
            e.setMantenimientos(dto.getMantenimientos().stream().map(pdto -> {
                Mantenimiento p = new Mantenimiento();
                p.setDescripcion(pdto.getDescripcion());
                p.setVehiculo(e);
                return p;
            }).collect(Collectors.toList()));
        }
        return e;
    }
}
