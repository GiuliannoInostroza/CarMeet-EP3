package com.carmeet.ms_ticketing.controller;

import com.carmeet.ms_ticketing.dto.TicketDTO;
import com.carmeet.ms_ticketing.dto.BeneficioDTO;
import com.carmeet.ms_ticketing.model.Beneficio;
import com.carmeet.ms_ticketing.model.Ticket;
import com.carmeet.ms_ticketing.service.TicketService;
import com.carmeet.ms_ticketing.security.JwtUtil;
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

@WebMvcTest(TicketController.class)
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private TicketService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void debeListarTickets() throws Exception {
        Ticket t = new Ticket(1L, 10L, 25.0, "VIP", "PENDIENTE", "user1", new ArrayList<>());
        when(service.listar()).thenReturn(List.of(t));

        mockMvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Listado"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].categoria").value("VIP"));
    }

    @Test
    void debeObtenerTicketPorId() throws Exception {
        Ticket t = new Ticket(1L, 10L, 25.0, "VIP", "PENDIENTE", "user1", new ArrayList<>());
        when(service.obtenerPorId(1L)).thenReturn(t);

        mockMvc.perform(get("/api/v1/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Encontrado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.categoria").value("VIP"));
    }

    @Test
    void debeGuardarTicket() throws Exception {
        TicketDTO reqDto = new TicketDTO();
        reqDto.setEventoId(10L);
        reqDto.setPrecio(25.0);
        reqDto.setCategoria("VIP");
        reqDto.setUsername("user1");

        BeneficioDTO bDto = new BeneficioDTO();
        bDto.setNombre("Acceso VIP");
        reqDto.setBeneficios(List.of(bDto));

        Ticket t = new Ticket(1L, 10L, 25.0, "VIP", "PENDIENTE", "user1", new ArrayList<>());
        Beneficio bObj = new Beneficio(100L, "Acceso VIP", "Desc", t);
        t.getBeneficios().add(bObj);
        
        when(service.guardar(any(Ticket.class), any())).thenReturn(t);

        mockMvc.perform(post("/api/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Ticket creado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.beneficios[0].nombre").value("Acceso VIP"));
    }

    @Test
    void debeActualizarTicket() throws Exception {
        TicketDTO reqDto = new TicketDTO();
        reqDto.setEventoId(10L);
        reqDto.setPrecio(30.0);
        reqDto.setCategoria("VIP");
        reqDto.setUsername("user1");
        reqDto.setEstado("PAGADO");
        reqDto.setBeneficios(null);

        Ticket t = new Ticket(1L, 10L, 30.0, "VIP", "PAGADO", "user1", null);

        when(service.actualizar(eq(1L), any(Ticket.class))).thenReturn(t);

        mockMvc.perform(put("/api/v1/tickets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Actualizado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeEliminarTicket() throws Exception {
        doNothing().when(service).eliminar(1L);

        mockMvc.perform(delete("/api/v1/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Eliminado"));
    }

    @Test
    void debeObtenerTicketsPorEvento() throws Exception {
        Ticket t = new Ticket(1L, 10L, 25.0, "VIP", "PENDIENTE", "user1", new ArrayList<>());
        when(service.obtenerPorEventoId(10L)).thenReturn(List.of(t));

        mockMvc.perform(get("/api/v1/tickets/evento/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tickets del evento 10"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void debeObtenerTicketsPorUsuario() throws Exception {
        Ticket t = new Ticket(1L, 10L, 25.0, "VIP", "PENDIENTE", "user1", new ArrayList<>());
        when(service.obtenerPorUsername("user1")).thenReturn(List.of(t));

        mockMvc.perform(get("/api/v1/tickets/usuario/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tickets del usuario: user1"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void debeCancelarTicket() throws Exception {
        Ticket t = new Ticket(1L, 10L, 25.0, "VIP", "CANCELADO", "user1", new ArrayList<>());
        when(service.cancelar(1L)).thenReturn(t);

        mockMvc.perform(patch("/api/v1/tickets/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Ticket cancelado"))
                .andExpect(jsonPath("$.data.estado").value("CANCELADO"));
    }

    @Test
    void debePagarTicket() throws Exception {
        Ticket t = new Ticket(1L, 10L, 25.0, "VIP", "PAGADO", "user1", new ArrayList<>());
        when(service.pagar(eq(1L), eq("TARJETA"), any())).thenReturn(t);

        Map<String, String> body = new HashMap<>();
        body.put("metodoPago", "TARJETA");

        mockMvc.perform(patch("/api/v1/tickets/1/pagar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pago procesado exitosamente"))
                .andExpect(jsonPath("$.data.estado").value("PAGADO"));
    }

    @Test
    void debePagarTicketSinBody() throws Exception {
        Ticket t = new Ticket(1L, 10L, 25.0, "VIP", "PAGADO", "user1", new ArrayList<>());
        when(service.pagar(eq(1L), eq("TARJETA"), any())).thenReturn(t);

        mockMvc.perform(patch("/api/v1/tickets/1/pagar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pago procesado exitosamente"))
                .andExpect(jsonPath("$.data.estado").value("PAGADO"));
    }
}
