package com.carmeet.ms_ticketing.service;

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

    public List<Ticket> listar() {
        return repo.findAll();
    }

    public Ticket obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con id: " + id));
    }

    public Ticket guardar(Ticket ticket) {
        if (ticket.getBeneficios() != null) {
            ticket.getBeneficios().forEach(b -> b.setTicket(ticket));
        }
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
}
