package com.carmeet.ms_payment_mock.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pago")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Pago {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long ticketId;
    private Double monto;
}
