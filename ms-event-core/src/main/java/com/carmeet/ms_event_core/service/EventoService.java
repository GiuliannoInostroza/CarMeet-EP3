package com.carmeet.ms_event_core.service;

import com.carmeet.ms_event_core.model.Evento;
import com.carmeet.ms_event_core.repository.EventoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository repo;

    public List<Evento> listar() {
        return repo.findAll();
    }

    public Evento obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Evento no encontrado con id: " + id));
    }

    public Evento guardar(Evento evento) {
        if (evento.getPatrocinadores() != null) {
            evento.getPatrocinadores().forEach(p -> p.setEvento(evento));
        }
        return repo.save(evento);
    }

    public Evento actualizar(Long id, Evento datosNuevos) {
        Evento eventoExistente = obtenerPorId(id);
        eventoExistente.setNombre(datosNuevos.getNombre());
        eventoExistente.setFecha(datosNuevos.getFecha());
        eventoExistente.setUbicacion(datosNuevos.getUbicacion());
        
        eventoExistente.getPatrocinadores().clear();
        if (datosNuevos.getPatrocinadores() != null) {
            datosNuevos.getPatrocinadores().forEach(p -> {
                p.setEvento(eventoExistente);
                eventoExistente.getPatrocinadores().add(p);
            });
        }
        
        return repo.save(eventoExistente);
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Evento no encontrado con id: " + id);
        }
        repo.deleteById(id);
    }
}
