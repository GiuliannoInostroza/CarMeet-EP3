package com.carmeet.ms_payment_mock.controller;

import com.carmeet.ms_payment_mock.dto.PagoDTO;
import com.carmeet.ms_payment_mock.dto.TransaccionLogDTO;
import com.carmeet.ms_payment_mock.model.Pago;
import com.carmeet.ms_payment_mock.model.TransaccionLog;
import com.carmeet.ms_payment_mock.service.PagoService;
import com.carmeet.ms_payment_mock.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private PagoService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void debeListarPagos() throws Exception {
        Pago p = new Pago(1L, 10L, 50.0, "CREDIT_CARD", new ArrayList<>());
        when(service.listar()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/pagos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Listado"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].monto").value(50.0));
    }

    @Test
    void debeObtenerPagoPorId() throws Exception {
        Pago p = new Pago(1L, 10L, 50.0, "CREDIT_CARD", new ArrayList<>());
        when(service.obtenerPorId(1L)).thenReturn(p);

        mockMvc.perform(get("/api/v1/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Encontrado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.monto").value(50.0));
    }

    @Test
    void debeGuardarPago() throws Exception {
        PagoDTO reqDto = new PagoDTO();
        reqDto.setTicketId(2L);
        reqDto.setMonto(150.0);
        reqDto.setMetodoPago("TARJETA");

        TransaccionLogDTO lDto = new TransaccionLogDTO();
        lDto.setEstado("APROBADO");
        reqDto.setLogs(List.of(lDto));

        Pago p = new Pago(1L, 2L, 150.0, "TARJETA", new ArrayList<>());
        TransaccionLog lObj = new TransaccionLog(10L, "APROBADO", LocalDateTime.now(), p);
        p.getLogs().add(lObj);
        
        when(service.guardar(any(Pago.class))).thenReturn(p);

        mockMvc.perform(post("/api/v1/pagos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Creado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.logs[0].estado").value("APROBADO"));
    }

    @Test
    void debeActualizarPago() throws Exception {
        PagoDTO reqDto = new PagoDTO();
        reqDto.setTicketId(10L);
        reqDto.setMonto(60.0);
        reqDto.setMetodoPago("CREDIT_CARD");

        Pago p = new Pago(1L, 10L, 60.0, "CREDIT_CARD", null);
        when(service.actualizar(eq(1L), any(Pago.class))).thenReturn(p);

        mockMvc.perform(put("/api/v1/pagos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Actualizado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeEliminarPago() throws Exception {
        doNothing().when(service).eliminar(1L);

        mockMvc.perform(delete("/api/v1/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Eliminado"));
    }

    @Test
    void debeObtenerPagoPorTicket() throws Exception {
        Pago p = new Pago(1L, 10L, 50.0, "CREDIT_CARD", new ArrayList<>());
        when(service.obtenerPorTicketId(10L)).thenReturn(p);

        mockMvc.perform(get("/api/v1/pagos/ticket/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pago del ticket 10"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeProcesarPago() throws Exception {
        PagoDTO reqDto = new PagoDTO();
        reqDto.setTicketId(10L);
        reqDto.setMonto(50.0);
        reqDto.setMetodoPago("CREDIT_CARD");

        Pago p = new Pago(1L, 10L, 50.0, "CREDIT_CARD", new ArrayList<>());
        when(service.procesarPago(any(Pago.class))).thenReturn(p);

        mockMvc.perform(post("/api/v1/pagos/procesar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pago procesado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeObtenerLogs() throws Exception {
        Pago p = new Pago(1L, 10L, 50.0, "CREDIT_CARD", new ArrayList<>());
        TransaccionLog log = new TransaccionLog(100L, "APPROVED", LocalDateTime.now(), p);
        when(service.obtenerLogs(1L)).thenReturn(List.of(log));

        mockMvc.perform(get("/api/v1/pagos/1/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logs del pago 1"))
                .andExpect(jsonPath("$.data.content[0].id").value(100))
                .andExpect(jsonPath("$.data.content[0].estado").value("APPROVED"));
    }
}
