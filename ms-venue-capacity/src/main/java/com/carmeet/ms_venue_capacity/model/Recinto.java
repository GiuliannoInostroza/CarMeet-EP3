package com.carmeet.ms_venue_capacity.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recinto")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Recinto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer capacidadMaxima;
    private Integer ocupacionActual;
}
