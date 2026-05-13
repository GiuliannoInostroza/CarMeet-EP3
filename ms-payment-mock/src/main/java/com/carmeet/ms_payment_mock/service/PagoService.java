package com.carmeet.ms_payment_mock.service;

import com.carmeet.ms_payment_mock.model.Pago;
import com.carmeet.ms_payment_mock.repository.PagoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository repo;

    public List<Pago> listar() {
        return repo.findAll();
    }

    public Pago obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Pago no encontrado con id: " + id));
    }

    public Pago guardar(Pago pago) {
        if (pago.getLogs() != null) {
            pago.getLogs().forEach(l -> l.setPago(pago));
        }
        return repo.save(pago);
    }

    public Pago actualizar(Long id, Pago datosNuevos) {
        Pago existente = obtenerPorId(id);
        existente.setTicketId(datosNuevos.getTicketId());
        existente.setMonto(datosNuevos.getMonto());
        
        existente.getLogs().clear();
        if (datosNuevos.getLogs() != null) {
            datosNuevos.getLogs().forEach(l -> {
                l.setPago(existente);
                existente.getLogs().add(l);
            });
        }
        
        return repo.save(existente);
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Pago no encontrado con id: " + id);
        }
        repo.deleteById(id);
    }
}
