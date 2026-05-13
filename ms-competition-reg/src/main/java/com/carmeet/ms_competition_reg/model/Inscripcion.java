package com.carmeet.ms_competition_reg.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inscripcion")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Inscripcion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long vehiculoId;
    private String categoria;
    private String username;
}
