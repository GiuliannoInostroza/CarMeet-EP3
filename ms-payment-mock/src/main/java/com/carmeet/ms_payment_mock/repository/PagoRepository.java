package com.carmeet.ms_payment_mock.repository;

import com.carmeet.ms_payment_mock.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {}
