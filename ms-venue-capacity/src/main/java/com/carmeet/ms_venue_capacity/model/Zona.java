package com.carmeet.ms_venue_capacity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "zona")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Zona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; // Ej: VIP, Galeria

    @Column(name = "capacidad", nullable = false)
    @Builder.Default
    private Integer capacidad = 0;

    @Column(name = "ocupacion", nullable = false)
    @Builder.Default
    private Integer ocupacion = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recinto_id")
    @JsonIgnore
    @ToString.Exclude
    private Recinto recinto;
}
