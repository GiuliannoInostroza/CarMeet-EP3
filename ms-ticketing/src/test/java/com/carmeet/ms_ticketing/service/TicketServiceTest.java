package com.carmeet.ms_ticketing.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.carmeet.ms_ticketing.client.EventoCoreClient;
import com.carmeet.ms_ticketing.client.NotificacionLogClient;
import com.carmeet.ms_ticketing.client.PagoMockClient;
import com.carmeet.ms_ticketing.model.Ticket;
import com.carmeet.ms_ticketing.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {
    @Mock
    private TicketRepository repo;

    @Mock
    private EventoCoreClient eventoClient;

    @Mock
    private PagoMockClient pagoClient;

    @Mock
    private NotificacionLogClient notificacionClient;

    @InjectMocks
    private TicketService service;

    @Test
    void listaDebeRetornarTodosLosTickets() {

    }

}
