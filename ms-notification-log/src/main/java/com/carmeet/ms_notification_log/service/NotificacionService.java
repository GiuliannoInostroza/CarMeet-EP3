package com.carmeet.ms_notification_log.service;

import com.carmeet.ms_notification_log.model.Notificacion;
import com.carmeet.ms_notification_log.repository.NotificacionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final NotificacionRepository repo;

    public List<Notificacion> listar() {
        return repo.findAll();
    }

    public Notificacion obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Notificacion no encontrada con id: " + id));
    }

    public Notificacion guardar(Notificacion notificacion) {
        if (notificacion.getAdjuntos() != null) {
            notificacion.getAdjuntos().forEach(a -> a.setNotificacion(notificacion));
        }
        if (notificacion.getLeida() == null) {
            notificacion.setLeida(false);
        }
        return repo.save(notificacion);
    }

    public Notificacion actualizar(Long id, Notificacion datosNuevos) {
        Notificacion existente = obtenerPorId(id);
        existente.setDestinatario(datosNuevos.getDestinatario());
        existente.setMensaje(datosNuevos.getMensaje());
        
        existente.getAdjuntos().clear();
        if (datosNuevos.getAdjuntos() != null) {
            datosNuevos.getAdjuntos().forEach(a -> {
                a.setNotificacion(existente);
                existente.getAdjuntos().add(a);
            });
        }
        
        return repo.save(existente);
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Notificacion no encontrada con id: " + id);
        }
        repo.deleteById(id);
    }

    /** Retorna notificaciones de un destinatario (usuario) */
    public List<Notificacion> obtenerPorDestinatario(String username) {
        return repo.findByDestinatario(username);
    }

    /**
     * Envía una notificación: la crea y simula el envío (log en consola).
     * Es llamado por otros microservicios vía WebClient.
     */
    public Notificacion enviar(Notificacion notificacion) {
        if (notificacion.getDestinatario() == null || notificacion.getDestinatario().isBlank()) {
            throw new RuntimeException("El destinatario es obligatorio para enviar una notificación");
        }
        if (notificacion.getMensaje() == null || notificacion.getMensaje().isBlank()) {
            throw new RuntimeException("El mensaje es obligatorio para enviar una notificación");
        }
        notificacion.setLeida(false);
        Notificacion guardada = guardar(notificacion);
        log.info("📧 Notificación enviada → [{}]: {}", guardada.getDestinatario(), guardada.getMensaje());
        return guardada;
    }

    /** Marca una notificación como leída */
    public Notificacion marcarLeida(Long id) {
        Notificacion notificacion = obtenerPorId(id);
        if (notificacion.getLeida()) {
            throw new RuntimeException("La notificación ya está marcada como leída");
        }
        notificacion.setLeida(true);
        return repo.save(notificacion);
    }
}
