package com.carmeet.ms_payment_mock.service;

import com.carmeet.ms_payment_mock.model.Pago;
import com.carmeet.ms_payment_mock.model.TransaccionLog;
import com.carmeet.ms_payment_mock.repository.PagoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

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
        existente.setMetodoPago(datosNuevos.getMetodoPago());  // BUG FIX: faltaba este campo

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

    /** Retorna el pago asociado a un ticketId */
    public Pago obtenerPorTicketId(Long ticketId) {
        return repo.findByTicketId(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró pago para el ticket: " + ticketId));
    }

    /**
     * Procesa el pago de forma simulada (mock).
     * Simula 80% aprobado, 20% rechazado.
     * Guarda el pago con el TransaccionLog del resultado.
     */
    public Pago procesarPago(Pago pago) {
        if (pago.getMonto() == null || pago.getMonto() <= 0) {
            throw new RuntimeException("El monto del pago debe ser mayor a 0");
        }

        // Simulación de resultado
        boolean aprobado = new Random().nextInt(100) < 80;
        String estadoResultado = aprobado ? "APROBADO" : "RECHAZADO";

        // Guardar log de la transacción
        TransaccionLog log = new TransaccionLog();
        log.setEstado(estadoResultado);
        log.setFecha(LocalDateTime.now());
        log.setPago(pago);

        pago.getLogs().add(log);

        Pago pagoProcesado = repo.save(pago);

        if (!aprobado) {
            throw new RuntimeException("Pago RECHAZADO por el procesador. Intente con otro método de pago.");
        }

        return pagoProcesado;
    }

    /** Retorna los logs de transacción de un pago */
    public List<TransaccionLog> obtenerLogs(Long pagoId) {
        Pago pago = obtenerPorId(pagoId);
        return pago.getLogs();
    }
}
