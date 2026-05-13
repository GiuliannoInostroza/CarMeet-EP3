package com.carmeet.ms_competition_reg.service;

import com.carmeet.ms_competition_reg.dto.InscripcionDTO;
import com.carmeet.ms_competition_reg.model.Inscripcion;
import com.carmeet.ms_competition_reg.repository.InscripcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InscripcionService {

    private final InscripcionRepository repo;

    public Inscripcion inscribir(InscripcionDTO dto) {
        Inscripcion i = Inscripcion.builder()
                .vehiculoId(dto.getVehiculoId())
                .categoria(dto.getCategoria())
                .username(SecurityContextHolder.getContext().getAuthentication().getName())
                .build();
                
        return repo.save(i);
    }
}
