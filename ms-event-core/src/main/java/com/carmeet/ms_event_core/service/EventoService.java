package com.carmeet.ms_event_core.service;

import com.carmeet.ms_event_core.model.Evento;
import com.carmeet.ms_event_core.dto.EventoDTO;
import com.carmeet.ms_event_core.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository repo;

    public Evento crear(EventoDTO dto) {
        Evento e = Evento.builder()
                .nombre(dto.getNombre())
                .fecha(dto.getFecha())
                .ubicacion(dto.getUbicacion())
                .build();
        return repo.save(e);
    }

    public List<Evento> listar() {
        return repo.findAll();
    }
}
