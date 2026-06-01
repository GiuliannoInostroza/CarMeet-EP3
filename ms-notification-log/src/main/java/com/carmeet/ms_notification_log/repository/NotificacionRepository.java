package com.carmeet.ms_notification_log.repository;

import com.carmeet.ms_notification_log.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByDestinatario(String destinatario);
    List<Notificacion> findByDestinatarioAndLeida(String destinatario, Boolean leida);
}
