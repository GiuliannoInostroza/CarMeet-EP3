package com.carmeet.ms_ticketing.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ticket")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long eventoId;
    private Double precio;
    private String estado;
    private String username;
}
