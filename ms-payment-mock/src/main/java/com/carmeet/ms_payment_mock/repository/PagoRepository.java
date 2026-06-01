package com.carmeet.ms_payment_mock.repository;

import com.carmeet.ms_payment_mock.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByTicketId(Long ticketId);
}
