package com.carmeet.ms_ticketing.service;

import com.carmeet.ms_ticketing.client.EventoCoreClient;
import com.carmeet.ms_ticketing.client.NotificacionLogClient;
import com.carmeet.ms_ticketing.client.PagoMockClient;
import com.carmeet.ms_ticketing.model.Ticket;
import com.carmeet.ms_ticketing.repository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository repo;
    private final EventoCoreClient eventoClient;
    private final PagoMockClient pagoClient;
    private final NotificacionLogClient notificacionClient;

    public List<Ticket> listar() {
        return repo.findAll();
    }

    public Ticket obtenerPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con id: " + id));
    }

    public Ticket guardar(Ticket ticket, String bearerToken) {
        eventoClient.validarEvento(ticket.getEventoId(), bearerToken);
        if (ticket.getBeneficios() != null) {
            ticket.getBeneficios().forEach(b -> b.setTicket(ticket));
        }
        ticket.setEstado("PENDIENTE");
        return repo.save(ticket);
    }

    public Ticket actualizar(Long id, Ticket datosNuevos) {
        Ticket existente = obtenerPorId(id);
        existente.setEventoId(datosNuevos.getEventoId());
        existente.setPrecio(datosNuevos.getPrecio());
        existente.setCategoria(datosNuevos.getCategoria());
        existente.setEstado(datosNuevos.getEstado());
        existente.setUsername(datosNuevos.getUsername());
        existente.getBeneficios().clear();
        if (datosNuevos.getBeneficios() != null) {
            datosNuevos.getBeneficios().forEach(b -> {
                b.setTicket(existente);
                existente.getBeneficios().add(b);
            });
        }
        return repo.save(existente);
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Ticket no encontrado con id: " + id);
        }
        repo.deleteById(id);
    }

    public List<Ticket> obtenerPorEventoId(Long eventoId) {
        return repo.findByEventoId(eventoId);
    }

    public List<Ticket> obtenerPorUsername(String username) {
        return repo.findByUsername(username);
    }

    public Ticket cancelar(Long id) {
        Ticket ticket = obtenerPorId(id);
        if (!"PENDIENTE".equals(ticket.getEstado())) {
            throw new RuntimeException(
                    "Solo se pueden cancelar tickets en estado PENDIENTE. Estado actual: " + ticket.getEstado());
        }
        ticket.setEstado("CANCELADO");
        return repo.save(ticket);
    }

    public Ticket pagar(Long id, String metodoPago, String bearerToken) {
        Ticket ticket = obtenerPorId(id);
        if (!"PENDIENTE".equals(ticket.getEstado())) {
            throw new RuntimeException(
                    "Solo se pueden pagar tickets en estado PENDIENTE. Estado actual: " + ticket.getEstado());
        }
        try {
            pagoClient.procesarPago(ticket.getId(), ticket.getPrecio(), metodoPago, bearerToken);
        } catch (Exception e) {
            throw new RuntimeException("Pago rechazado: " + e.getMessage());
        }
        ticket.setEstado("PAGADO");
        Ticket guardado = repo.save(ticket);
        if (ticket.getUsername() != null) {
            notificacionClient.enviar(
                    ticket.getUsername(),
                    "Tu ticket #" + ticket.getId() + " ha sido PAGADO. Precio: $" + ticket.getPrecio(),
                    bearerToken);
        }
        return guardado;
    }
}
