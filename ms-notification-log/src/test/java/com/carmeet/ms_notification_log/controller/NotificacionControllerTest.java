package com.carmeet.ms_notification_log.controller;

import com.carmeet.ms_notification_log.dto.NotificacionDTO;
import com.carmeet.ms_notification_log.dto.AdjuntoDTO;
import com.carmeet.ms_notification_log.model.Notificacion;
import com.carmeet.ms_notification_log.model.Adjunto;
import com.carmeet.ms_notification_log.service.NotificacionService;
import com.carmeet.ms_notification_log.security.JwtUtil;
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

@WebMvcTest(NotificacionController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private NotificacionService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void debeListarNotificaciones() throws Exception {
        Notificacion n = new Notificacion(1L, "dest@mail.com", "Hello", false, new ArrayList<>());
        when(service.listar()).thenReturn(List.of(n));

        mockMvc.perform(get("/api/v1/notificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Listado"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].destinatario").value("dest@mail.com"));
    }

    @Test
    void debeObtenerNotificacionPorId() throws Exception {
        Notificacion n = new Notificacion(1L, "dest@mail.com", "Hello", false, new ArrayList<>());
        when(service.obtenerPorId(1L)).thenReturn(n);

        mockMvc.perform(get("/api/v1/notificaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Encontrado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.destinatario").value("dest@mail.com"));
    }

    @Test
    void debeGuardarNotificacion() throws Exception {
        NotificacionDTO reqDto = new NotificacionDTO();
        reqDto.setDestinatario("dest@mail.com");
        reqDto.setMensaje("Hello");
        reqDto.setLeida(false);

        AdjuntoDTO aDto = new AdjuntoDTO();
        aDto.setNombreArchivo("file.txt");
        aDto.setUrl("http://file.com");
        reqDto.setAdjuntos(List.of(aDto));

        Notificacion n = new Notificacion(1L, "dest@mail.com", "Hello", false, new ArrayList<>());
        Adjunto a = new Adjunto(10L, "file.txt", "url", "path", n);
        n.getAdjuntos().add(a);
        
        when(service.guardar(any(Notificacion.class))).thenReturn(n);

        mockMvc.perform(post("/api/v1/notificaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Creado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.adjuntos[0].nombreArchivo").value("file.txt"));
    }

    @Test
    void debeActualizarNotificacion() throws Exception {
        NotificacionDTO reqDto = new NotificacionDTO();
        reqDto.setDestinatario("dest@mail.com");
        reqDto.setMensaje("Hello Updated");
        reqDto.setLeida(null);

        Notificacion n = new Notificacion(1L, "dest@mail.com", "Hello Updated", false, null);
        when(service.actualizar(eq(1L), any(Notificacion.class))).thenReturn(n);

        mockMvc.perform(put("/api/v1/notificaciones/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Actualizado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeEliminarNotificacion() throws Exception {
        doNothing().when(service).eliminar(1L);

        mockMvc.perform(delete("/api/v1/notificaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Eliminado"));
    }

    @Test
    void debeObtenerNotificacionesPorDestinatario() throws Exception {
        Notificacion n = new Notificacion(1L, "dest@mail.com", "Hello", false, new ArrayList<>());
        when(service.obtenerPorDestinatario("dest@mail.com")).thenReturn(List.of(n));

        mockMvc.perform(get("/api/v1/notificaciones/destinatario/dest@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notificaciones de: dest@mail.com"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void debeEnviarNotificacion() throws Exception {
        NotificacionDTO reqDto = new NotificacionDTO();
        reqDto.setDestinatario("dest@mail.com");
        reqDto.setMensaje("Hello");

        Notificacion n = new Notificacion(1L, "dest@mail.com", "Hello", false, new ArrayList<>());
        when(service.enviar(any(Notificacion.class))).thenReturn(n);

        mockMvc.perform(post("/api/v1/notificaciones/enviar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notificación enviada"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeMarcarLeida() throws Exception {
        Notificacion n = new Notificacion(1L, "dest@mail.com", "Hello", true, new ArrayList<>());
        when(service.marcarLeida(1L)).thenReturn(n);

        mockMvc.perform(patch("/api/v1/notificaciones/1/marcar-leida"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notificación marcada como leída"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.leida").value(true));
    }
}
