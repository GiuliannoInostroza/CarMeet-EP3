package com.carmeet.ms_live_scoreboard.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "puntuacion")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Puntuacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long inscripcionId;
    private Integer puntos;
}
