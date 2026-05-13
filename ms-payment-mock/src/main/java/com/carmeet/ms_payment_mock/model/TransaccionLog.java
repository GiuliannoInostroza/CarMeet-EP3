package com.carmeet.ms_payment_mock.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaccion_log")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TransaccionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String estado;
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id")
    @JsonIgnore
    @ToString.Exclude
    private Pago pago;
}
