package com.carmeet.ms_analytics_report.controller;

import com.carmeet.ms_analytics_report.dto.ReporteDTO;
import com.carmeet.ms_analytics_report.dto.MetricaDTO;
import com.carmeet.ms_analytics_report.model.Reporte;
import com.carmeet.ms_analytics_report.model.Metrica;
import com.carmeet.ms_analytics_report.service.AnalyticsService;
import com.carmeet.ms_analytics_report.security.JwtUtil;
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

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private AnalyticsService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void debeListarReportes() throws Exception {
        Reporte r = new Reporte(1L, 10L, 5, 20, 15, LocalDate.now().toString(), new ArrayList<>());
        when(service.listar()).thenReturn(List.of(r));

        mockMvc.perform(get("/api/v1/reportes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Listado"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].eventoId").value(10));
    }

    @Test
    void debeObtenerReportePorId() throws Exception {
        Reporte r = new Reporte(1L, 10L, 5, 20, 15, LocalDate.now().toString(), new ArrayList<>());
        when(service.obtenerPorId(1L)).thenReturn(r);

        mockMvc.perform(get("/api/v1/reportes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Encontrado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.eventoId").value(10));
    }

    @Test
    void debeGuardarReporte() throws Exception {
        ReporteDTO reqDto = new ReporteDTO();
        reqDto.setEventoId(10L);
        reqDto.setTotalEventos(5);
        reqDto.setTotalTickets(20);
        reqDto.setTotalInscripciones(15);
        reqDto.setFechaGeneracion(LocalDate.now().toString());

        MetricaDTO mDto = new MetricaDTO();
        mDto.setNombre("TEST_METRIC");
        mDto.setValor(100.0);
        reqDto.setMetricas(List.of(mDto));

        Reporte r = new Reporte(1L, 10L, 5, 20, 15, LocalDate.now().toString(), new ArrayList<>());
        Metrica m = new Metrica(100L, "TEST_METRIC", 100.0, r);
        r.getMetricas().add(m);

        when(service.guardar(any(Reporte.class))).thenReturn(r);

        mockMvc.perform(post("/api/v1/reportes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Creado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.metricas[0].nombre").value("TEST_METRIC"));
    }

    @Test
    void debeActualizarReporte() throws Exception {
        ReporteDTO reqDto = new ReporteDTO();
        reqDto.setEventoId(10L);
        reqDto.setTotalEventos(5);
        reqDto.setTotalTickets(20);
        reqDto.setTotalInscripciones(15);
        reqDto.setFechaGeneracion(LocalDate.now().toString());

        Reporte r = new Reporte(1L, 10L, 5, 20, 15, LocalDate.now().toString(), null);
        when(service.actualizar(eq(1L), any(Reporte.class))).thenReturn(r);

        mockMvc.perform(put("/api/v1/reportes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Actualizado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeEliminarReporte() throws Exception {
        doNothing().when(service).eliminar(1L);

        mockMvc.perform(delete("/api/v1/reportes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Eliminado"));
    }

    @Test
    void debeGenerarReporte() throws Exception {
        Reporte r = new Reporte(1L, 10L, 5, 20, 15, LocalDate.now().toString(), new ArrayList<>());
        when(service.generarReporte(eq(10L), any())).thenReturn(r);

        mockMvc.perform(post("/api/v1/reportes/generar/10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reporte generado para el evento 10"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeObtenerResumenMetricas() throws Exception {
        MetricaDTO mDto = new MetricaDTO();
        mDto.setNombre("TOTAL");
        mDto.setValor(500.0);

        when(service.resumenMetricas()).thenReturn(List.of(mDto));

        mockMvc.perform(get("/api/v1/reportes/metricas/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resumen de métricas"))
                .andExpect(jsonPath("$.data.content[0].nombre").value("TOTAL"));
    }
}
