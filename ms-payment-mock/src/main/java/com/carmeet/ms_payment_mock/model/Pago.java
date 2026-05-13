package com.carmeet.ms_payment_mock.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "pago")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Pago {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long ticketId;
    private Double monto;

    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransaccionLog> logs = new ArrayList<>();
}
