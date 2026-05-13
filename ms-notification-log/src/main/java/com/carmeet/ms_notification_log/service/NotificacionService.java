package com.carmeet.ms_notification_log.service;

import com.carmeet.ms_notification_log.model.Notificacion;
import com.carmeet.ms_notification_log.repository.NotificacionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
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
}
