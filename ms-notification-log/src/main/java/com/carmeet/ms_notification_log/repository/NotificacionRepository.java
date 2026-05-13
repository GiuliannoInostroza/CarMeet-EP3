package com.carmeet.ms_notification_log.repository;

import com.carmeet.ms_notification_log.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {}
