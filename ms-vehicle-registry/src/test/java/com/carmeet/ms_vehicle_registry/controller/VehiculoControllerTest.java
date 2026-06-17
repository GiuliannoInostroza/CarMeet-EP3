package com.carmeet.ms_vehicle_registry.controller;

import com.carmeet.ms_vehicle_registry.dto.VehiculoDTO;
import com.carmeet.ms_vehicle_registry.dto.MantenimientoDTO;
import com.carmeet.ms_vehicle_registry.model.Vehiculo;
import com.carmeet.ms_vehicle_registry.model.Mantenimiento;
import com.carmeet.ms_vehicle_registry.service.VehiculoService;
import com.carmeet.ms_vehicle_registry.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehiculoController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private VehiculoService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void debeListarVehiculos() throws Exception {
        Vehiculo v = new Vehiculo(1L, "Toyota", "Supra", 2020, new ArrayList<>());
        when(service.listar()).thenReturn(List.of(v));

        mockMvc.perform(get("/api/v1/vehiculos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Listado"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].modelo").value("Supra"));
    }

    @Test
    void debeObtenerVehiculoPorId() throws Exception {
        Vehiculo v = new Vehiculo(1L, "Toyota", "Supra", 2020, new ArrayList<>());
        when(service.obtenerPorId(1L)).thenReturn(v);

        mockMvc.perform(get("/api/v1/vehiculos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Encontrado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.modelo").value("Supra"));
    }

    @Test
    void debeGuardarVehiculo() throws Exception {
        VehiculoDTO reqDto = new VehiculoDTO();
        reqDto.setMarca("Toyota");
        reqDto.setModelo("Supra");
        reqDto.setAnio(2020);

        MantenimientoDTO mDto = new MantenimientoDTO();
        mDto.setDescripcion("Cambio de aceite");
        reqDto.setMantenimientos(List.of(mDto));

        Vehiculo v = new Vehiculo(1L, "Toyota", "Supra", 2020, new ArrayList<>());
        Mantenimiento mObj = new Mantenimiento(10L, "Cambio de aceite", v);
        v.getMantenimientos().add(mObj);
        
        when(service.guardar(any(Vehiculo.class))).thenReturn(v);

        mockMvc.perform(post("/api/v1/vehiculos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Creado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.mantenimientos[0].descripcion").value("Cambio de aceite"));
    }

    @Test
    void debeActualizarVehiculo() throws Exception {
        VehiculoDTO reqDto = new VehiculoDTO();
        reqDto.setMarca("Toyota");
        reqDto.setModelo("Supra Updated");
        reqDto.setAnio(2020);

        Vehiculo v = new Vehiculo(1L, "Toyota", "Supra Updated", 2020, null);
        when(service.actualizar(eq(1L), any(Vehiculo.class))).thenReturn(v);

        mockMvc.perform(put("/api/v1/vehiculos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Actualizado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeEliminarVehiculo() throws Exception {
        doNothing().when(service).eliminar(1L);

        mockMvc.perform(delete("/api/v1/vehiculos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Eliminado"));
    }

    @Test
    void debeListarMantenimientos() throws Exception {
        Vehiculo v = new Vehiculo(1L, "Toyota", "Supra", 2020, new ArrayList<>());
        Mantenimiento m = new Mantenimiento(10L, "Cambio de aceite", v);
        when(service.listarMantenimientos(1L)).thenReturn(List.of(m));

        mockMvc.perform(get("/api/v1/vehiculos/1/mantenimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Historial de mantenimientos"))
                .andExpect(jsonPath("$.data.content[0].id").value(10))
                .andExpect(jsonPath("$.data.content[0].descripcion").value("Cambio de aceite"));
    }

    @Test
    void debeAgregarMantenimiento() throws Exception {
        MantenimientoDTO reqDto = new MantenimientoDTO();
        reqDto.setDescripcion("Cambio de frenos");

        Vehiculo v = new Vehiculo(1L, "Toyota", "Supra", 2020, new ArrayList<>());
        when(service.agregarMantenimiento(eq(1L), any(Mantenimiento.class))).thenReturn(v);

        mockMvc.perform(post("/api/v1/vehiculos/1/mantenimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Mantenimiento agregado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeBuscarVehiculosPorModelo() throws Exception {
        Vehiculo v = new Vehiculo(1L, "Toyota", "Supra", 2020, new ArrayList<>());
        when(service.buscarPorModelo("Supra")).thenReturn(List.of(v));

        mockMvc.perform(get("/api/v1/vehiculos/buscar?modelo=Supra"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resultados para: Supra"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }
}
