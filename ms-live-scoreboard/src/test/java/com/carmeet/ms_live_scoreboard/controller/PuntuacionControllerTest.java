package com.carmeet.ms_live_scoreboard.controller;

import com.carmeet.ms_live_scoreboard.dto.PuntuacionDTO;
import com.carmeet.ms_live_scoreboard.model.Puntuacion;
import com.carmeet.ms_live_scoreboard.service.PuntuacionService;
import com.carmeet.ms_live_scoreboard.security.JwtUtil;
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

@WebMvcTest(PuntuacionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PuntuacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private PuntuacionService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void debeListarPuntuaciones() throws Exception {
        Puntuacion p = new Puntuacion(1L, 2L, 3L, 85, new ArrayList<>());
        when(service.listar()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/puntuaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Listado"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].puntos").value(85));
    }

    @Test
    void debeObtenerPuntuacionPorId() throws Exception {
        Puntuacion p = new Puntuacion(1L, 2L, 3L, 85, new ArrayList<>());
        when(service.obtenerPorId(1L)).thenReturn(p);

        mockMvc.perform(get("/api/v1/puntuaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Encontrado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.puntos").value(85));
    }

    @Test
    void debeGuardarPuntuacion() throws Exception {
        PuntuacionDTO reqDto = new PuntuacionDTO();
        reqDto.setInscripcionId(2L);
        reqDto.setEventoId(3L);
        reqDto.setPuntos(85);

        Puntuacion p = new Puntuacion(1L, 2L, 3L, 85, new ArrayList<>());
        when(service.guardar(any(Puntuacion.class), any())).thenReturn(p);

        mockMvc.perform(post("/api/v1/puntuaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Puntuación registrada"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeActualizarPuntuacion() throws Exception {
        PuntuacionDTO reqDto = new PuntuacionDTO();
        reqDto.setInscripcionId(2L);
        reqDto.setEventoId(3L);
        reqDto.setPuntos(90);

        Puntuacion p = new Puntuacion(1L, 2L, 3L, 90, new ArrayList<>());
        when(service.actualizar(eq(1L), any(Puntuacion.class))).thenReturn(p);

        mockMvc.perform(put("/api/v1/puntuaciones/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Actualizado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeEliminarPuntuacion() throws Exception {
        doNothing().when(service).eliminar(1L);

        mockMvc.perform(delete("/api/v1/puntuaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Eliminado"));
    }

    @Test
    void debeObtenerRankingPorEvento() throws Exception {
        Puntuacion p = new Puntuacion(1L, 2L, 3L, 85, new ArrayList<>());
        when(service.rankingPorEvento(3L)).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/puntuaciones/evento/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Ranking del evento 3"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void debeObtenerPuntuacionesPorInscripcion() throws Exception {
        Puntuacion p = new Puntuacion(1L, 2L, 3L, 85, new ArrayList<>());
        when(service.porInscripcion(2L)).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/puntuaciones/inscripcion/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Puntuaciones de inscripción 2"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void debeObtenerTop10Global() throws Exception {
        Puntuacion p = new Puntuacion(1L, 2L, 3L, 85, new ArrayList<>());
        when(service.top10Global()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/puntuaciones/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Top 10 puntuaciones globales"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }
}
