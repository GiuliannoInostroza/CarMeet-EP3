package com.carmeet.ms_live_scoreboard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detalle_puntuacion")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DetallePuntuacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categoria; // Ej: Drift, Velocidad
    private Integer puntosAsignados;
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puntuacion_id")
    @JsonIgnore
    @ToString.Exclude
    private Puntuacion puntuacion;
}
