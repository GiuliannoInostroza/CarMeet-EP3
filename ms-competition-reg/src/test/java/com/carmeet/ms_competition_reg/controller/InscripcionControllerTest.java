package com.carmeet.ms_competition_reg.controller;

import com.carmeet.ms_competition_reg.dto.InscripcionDTO;
import com.carmeet.ms_competition_reg.model.Inscripcion;
import com.carmeet.ms_competition_reg.service.InscripcionService;
import com.carmeet.ms_competition_reg.security.JwtUtil;
import com.carmeet.ms_competition_reg.dto.RequisitoDTO;
import com.carmeet.ms_competition_reg.model.Requisito;
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

@WebMvcTest(InscripcionController.class)
@AutoConfigureMockMvc(addFilters = false)
class InscripcionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private InscripcionService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void debeListarInscripciones() throws Exception {
        Inscripcion i = new Inscripcion(1L, 2L, 3L, "Juan Perez", "Tuning", "juanp", "PENDIENTE", new ArrayList<>());
        when(service.listar()).thenReturn(List.of(i));

        mockMvc.perform(get("/api/v1/inscripciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Listado"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].participante").value("Juan Perez"));
    }

    @Test
    void debeObtenerInscripcionPorId() throws Exception {
        Inscripcion i = new Inscripcion(1L, 2L, 3L, "Juan Perez", "Tuning", "juanp", "PENDIENTE", new ArrayList<>());
        when(service.obtenerPorId(1L)).thenReturn(i);

        mockMvc.perform(get("/api/v1/inscripciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Encontrado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.participante").value("Juan Perez"));
    }

    @Test
    void debeGuardarInscripcion() throws Exception {
        InscripcionDTO reqDto = new InscripcionDTO();
        reqDto.setVehiculoId(2L);
        reqDto.setEventoId(3L);
        reqDto.setParticipante("Juan Perez");
        reqDto.setCategoria("Tuning");
        reqDto.setUsername("juanp");

        RequisitoDTO reqReqDto = new RequisitoDTO();
        reqReqDto.setNombre("Req 1");
        reqReqDto.setDescripcion("Desc 1");
        reqDto.setRequisitos(List.of(reqReqDto));

        Inscripcion i = new Inscripcion(1L, 2L, 3L, "Juan Perez", "Tuning", "juanp", "PENDIENTE", new ArrayList<>());
        Requisito reqObj = new Requisito();
        reqObj.setId(1L);
        reqObj.setNombre("Req 1");
        reqObj.setDescripcion("Desc 1");
        i.getRequisitos().add(reqObj);

        when(service.guardar(any(Inscripcion.class), any())).thenReturn(i);

        mockMvc.perform(post("/api/v1/inscripciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Inscripción creada"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.requisitos[0].nombre").value("Req 1"));
    }

    @Test
    void debeActualizarInscripcion() throws Exception {
        InscripcionDTO reqDto = new InscripcionDTO();
        reqDto.setVehiculoId(2L);
        reqDto.setEventoId(3L);
        reqDto.setParticipante("Juan Perez");
        reqDto.setCategoria("Tuning");
        reqDto.setUsername("juanp");
        reqDto.setEstado("ACTIVA");
        reqDto.setRequisitos(null);

        Inscripcion i = new Inscripcion(1L, 2L, 3L, "Juan Perez", "Tuning", "juanp", "ACTIVA", null);

        when(service.actualizar(eq(1L), any(Inscripcion.class))).thenReturn(i);

        mockMvc.perform(put("/api/v1/inscripciones/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Actualizado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeEliminarInscripcion() throws Exception {
        doNothing().when(service).eliminar(1L);

        mockMvc.perform(delete("/api/v1/inscripciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Eliminado"));
    }

    @Test
    void debeObtenerInscripcionesPorEvento() throws Exception {
        Inscripcion i = new Inscripcion(1L, 2L, 3L, "Juan Perez", "Tuning", "juanp", "PENDIENTE", new ArrayList<>());
        when(service.obtenerPorEventoId(3L)).thenReturn(List.of(i));

        mockMvc.perform(get("/api/v1/inscripciones/evento/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Inscripciones del evento 3"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void debeObtenerInscripcionesPorVehiculo() throws Exception {
        Inscripcion i = new Inscripcion(1L, 2L, 3L, "Juan Perez", "Tuning", "juanp", "PENDIENTE", new ArrayList<>());
        when(service.obtenerPorVehiculoId(2L)).thenReturn(List.of(i));

        mockMvc.perform(get("/api/v1/inscripciones/vehiculo/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Inscripciones del vehículo 2"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void debeAprobarInscripcion() throws Exception {
        Inscripcion i = new Inscripcion(1L, 2L, 3L, "Juan Perez", "Tuning", "juanp", "APROBADA", new ArrayList<>());
        when(service.aprobar(eq(1L), any())).thenReturn(i);

        mockMvc.perform(patch("/api/v1/inscripciones/1/aprobar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Inscripción aprobada"))
                .andExpect(jsonPath("$.data.estado").value("APROBADA"));
    }

    @Test
    void debeRechazarInscripcion() throws Exception {
        Inscripcion i = new Inscripcion(1L, 2L, 3L, "Juan Perez", "Tuning", "juanp", "RECHAZADA", new ArrayList<>());
        when(service.rechazar(eq(1L), any())).thenReturn(i);

        mockMvc.perform(patch("/api/v1/inscripciones/1/rechazar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Inscripción rechazada"))
                .andExpect(jsonPath("$.data.estado").value("RECHAZADA"));
    }
}
