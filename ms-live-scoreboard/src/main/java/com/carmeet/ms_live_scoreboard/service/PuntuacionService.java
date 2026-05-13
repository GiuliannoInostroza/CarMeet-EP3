package com.carmeet.ms_live_scoreboard.service;

import com.carmeet.ms_live_scoreboard.dto.PuntuacionDTO;
import com.carmeet.ms_live_scoreboard.model.Puntuacion;
import org.springframework.stereotype.Service;

@Service
public class PuntuacionService {

    public Puntuacion registrarPuntos(PuntuacionDTO dto) {
        return Puntuacion.builder()
                .inscripcionId(dto.getInscripcionId())
                .puntos(dto.getPuntos())
                .build();
    }
}
