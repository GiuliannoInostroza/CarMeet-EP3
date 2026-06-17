package com.carmeet.ms_venue_capacity.controller;

import com.carmeet.ms_venue_capacity.dto.RecintoDTO;
import com.carmeet.ms_venue_capacity.model.Recinto;
import com.carmeet.ms_venue_capacity.dto.ZonaDTO;
import com.carmeet.ms_venue_capacity.model.Zona;
import com.carmeet.ms_venue_capacity.service.RecintoService;
import com.carmeet.ms_venue_capacity.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecintoController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecintoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private RecintoService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void debeListarRecintos() throws Exception {
        Recinto r = new Recinto(1L, "Stadium A", 500, 100, new ArrayList<>());
        when(service.listar()).thenReturn(List.of(r));

        mockMvc.perform(get("/api/v1/recintos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Listado"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].nombre").value("Stadium A"));
    }

    @Test
    void debeObtenerRecintoPorId() throws Exception {
        Recinto r = new Recinto(1L, "Stadium A", 500, 100, new ArrayList<>());
        when(service.obtenerPorId(1L)).thenReturn(r);

        mockMvc.perform(get("/api/v1/recintos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Encontrado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nombre").value("Stadium A"));
    }

    @Test
    void debeGuardarRecinto() throws Exception {
        RecintoDTO reqDto = new RecintoDTO();
        reqDto.setNombre("Stadium A");
        reqDto.setCapacidadMaxima(500);
        reqDto.setOcupacionActual(100);

        ZonaDTO zDto = new ZonaDTO();
        zDto.setNombre("VIP Zone");
        reqDto.setZonas(List.of(zDto));

        Recinto r = new Recinto(1L, "Stadium A", 500, 100, new ArrayList<>());
        Zona zObj = new Zona(10L, "VIP Zone", 100, 20, r);
        r.getZonas().add(zObj);
        
        when(service.guardar(any(Recinto.class))).thenReturn(r);

        mockMvc.perform(post("/api/v1/recintos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Creado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.zonas[0].nombre").value("VIP Zone"));
    }

    @Test
    void debeActualizarRecinto() throws Exception {
        RecintoDTO reqDto = new RecintoDTO();
        reqDto.setNombre("Stadium A Updated");
        reqDto.setCapacidadMaxima(null);
        reqDto.setOcupacionActual(null);

        Recinto r = new Recinto(1L, "Stadium A Updated", 500, 100, null);
        when(service.actualizar(eq(1L), any(Recinto.class))).thenReturn(r);

        mockMvc.perform(put("/api/v1/recintos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Actualizado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeEliminarRecinto() throws Exception {
        doNothing().when(service).eliminar(1L);

        mockMvc.perform(delete("/api/v1/recintos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Eliminado"));
    }

    @Test
    void debeListarZonas() throws Exception {
        Recinto r = new Recinto(1L, "Stadium A", 500, 100, new ArrayList<>());
        Zona z = new Zona(10L, "VIP Zone", 100, 20, r);
        when(service.listarZonas(1L)).thenReturn(List.of(z));

        mockMvc.perform(get("/api/v1/recintos/1/zonas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Zonas del recinto 1"))
                .andExpect(jsonPath("$.data.content[0].id").value(10))
                .andExpect(jsonPath("$.data.content[0].nombre").value("VIP Zone"));
    }

    @Test
    void debeConsultarDisponibilidad() throws Exception {
        Map<String, Object> mockDisp = new HashMap<>();
        mockDisp.put("id", 1L);
        mockDisp.put("nombre", "Stadium A");
        mockDisp.put("capacidadMaxima", 500);
        mockDisp.put("ocupacionActual", 100);
        mockDisp.put("plazasLibres", 400);
        mockDisp.put("disponible", true);

        when(service.consultarDisponibilidad(1L)).thenReturn(mockDisp);

        mockMvc.perform(get("/api/v1/recintos/1/disponibilidad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Disponibilidad del recinto"))
                .andExpect(jsonPath("$.data.nombre").value("Stadium A"))
                .andExpect(jsonPath("$.data.plazasLibres").value(400));
    }

    @Test
    void debeRegistrarIngreso() throws Exception {
        Recinto r = new Recinto(1L, "Stadium A", 500, 101, new ArrayList<>());
        when(service.registrarIngreso(1L)).thenReturn(r);

        mockMvc.perform(post("/api/v1/recintos/1/registrar-ingreso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Ingreso registrado"))
                .andExpect(jsonPath("$.data.ocupacionActual").value(101));
    }

    @Test
    void debeRegistrarEgreso() throws Exception {
        Recinto r = new Recinto(1L, "Stadium A", 500, 99, new ArrayList<>());
        when(service.registrarEgreso(1L)).thenReturn(r);

        mockMvc.perform(post("/api/v1/recintos/1/registrar-egreso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Egreso registrado"))
                .andExpect(jsonPath("$.data.ocupacionActual").value(99));
    }
}
