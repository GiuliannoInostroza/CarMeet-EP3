package com.carmeet.ms_event_core.controller;

import com.carmeet.ms_event_core.dto.EventoDTO;
import com.carmeet.ms_event_core.model.Evento;
import com.carmeet.ms_event_core.model.Patrocinador;
import com.carmeet.ms_event_core.service.EventoService;
import com.carmeet.ms_event_core.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventoController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private EventoService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void debeListarEventos() throws Exception {
        Evento ev = new Evento(1L, "Exposition Car", LocalDate.now().toString(), "Santiago", new ArrayList<>());
        when(service.listar()).thenReturn(List.of(ev));

        mockMvc.perform(get("/api/v1/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Listado"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].nombre").value("Exposition Car"));
    }

    @Test
    void debeObtenerEventoPorId() throws Exception {
        Evento ev = new Evento(1L, "Exposition Car", LocalDate.now().toString(), "Santiago", new ArrayList<>());
        when(service.obtenerPorId(1L)).thenReturn(ev);

        mockMvc.perform(get("/api/v1/eventos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Encontrado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nombre").value("Exposition Car"));
    }

    @Test
    void debeGuardarEvento() throws Exception {
        EventoDTO reqDto = new EventoDTO();
        reqDto.setNombre("Exposition Car");
        reqDto.setFecha(LocalDate.now().toString());
        reqDto.setUbicacion("Santiago");

        Evento ev = new Evento(1L, "Exposition Car", LocalDate.now().toString(), "Santiago", new ArrayList<>());
        when(service.guardar(any(Evento.class))).thenReturn(ev);

        mockMvc.perform(post("/api/v1/eventos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Creado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeActualizarEvento() throws Exception {
        EventoDTO reqDto = new EventoDTO();
        reqDto.setNombre("Exposition Car Updated");
        reqDto.setFecha(LocalDate.now().toString());
        reqDto.setUbicacion("Santiago");

        Evento ev = new Evento(1L, "Exposition Car Updated", LocalDate.now().toString(), "Santiago", new ArrayList<>());
        when(service.actualizar(eq(1L), any(Evento.class))).thenReturn(ev);

        mockMvc.perform(put("/api/v1/eventos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Actualizado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeEliminarEvento() throws Exception {
        doNothing().when(service).eliminar(1L);

        mockMvc.perform(delete("/api/v1/eventos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Eliminado"));
    }

    @Test
    void debeListarPatrocinadores() throws Exception {
        Evento ev = new Evento(1L, "Exposition Car", LocalDate.now().toString(), "Santiago", new ArrayList<>());
        Patrocinador p = new Patrocinador(10L, "Brand A", "Gold", ev);
        ev.getPatrocinadores().add(p);
        when(service.obtenerPorId(1L)).thenReturn(ev);

        mockMvc.perform(get("/api/v1/eventos/1/patrocinadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Patrocinadores del evento 1"))
                .andExpect(jsonPath("$.data.content[0].id").value(10))
                .andExpect(jsonPath("$.data.content[0].nombre").value("Brand A"));
    }

    @Test
    void debeListarEventosProximos() throws Exception {
        Evento ev = new Evento(1L, "Exposition Car", LocalDate.now().toString(), "Santiago", new ArrayList<>());
        when(service.listarProximos()).thenReturn(List.of(ev));

        mockMvc.perform(get("/api/v1/eventos/proximos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Eventos próximos"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void debeBuscarEventosPorNombre() throws Exception {
        Evento ev = new Evento(1L, "Exposition Car", LocalDate.now().toString(), "Santiago", new ArrayList<>());
        when(service.buscarPorNombre("Exposition")).thenReturn(List.of(ev));

        mockMvc.perform(get("/api/v1/eventos/buscar?nombre=Exposition"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resultados para: Exposition"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }
}
