package com.carmeet.ms_event_core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "patrocinador")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Patrocinador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String nivel; // Ejemplo: ORO, PLATA, BRONCE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id")
    @JsonIgnore
    @ToString.Exclude
    private Evento evento;
}
