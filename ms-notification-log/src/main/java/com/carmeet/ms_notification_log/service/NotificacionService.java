package com.carmeet.ms_notification_log.service;

import com.carmeet.ms_notification_log.dto.NotificacionDTO;
import com.carmeet.ms_notification_log.model.Notificacion;
import com.carmeet.ms_notification_log.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository repo;

    public void enviar(NotificacionDTO dto) {
        log.info("Simulando envío de correo a {}: {}", dto.getDestinatario(), dto.getMensaje());
        
        Notificacion n = Notificacion.builder()
                .destinatario(dto.getDestinatario())
                .mensaje(dto.getMensaje())
                .build();
                
        repo.save(n);
    }
}
