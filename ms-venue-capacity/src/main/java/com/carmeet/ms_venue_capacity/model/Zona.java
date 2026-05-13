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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recinto_id")
    @JsonIgnore
    @ToString.Exclude
    private Recinto recinto;
}
