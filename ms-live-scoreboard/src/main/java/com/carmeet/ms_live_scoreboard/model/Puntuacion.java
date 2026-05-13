package com.carmeet.ms_live_scoreboard.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "puntuacion")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Puntuacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long inscripcionId;
    private Integer puntos;

    @OneToMany(mappedBy = "puntuacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetallePuntuacion> detalles = new ArrayList<>();
}
