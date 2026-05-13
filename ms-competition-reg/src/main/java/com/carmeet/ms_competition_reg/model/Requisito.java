package com.carmeet.ms_competition_reg.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "requisito")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Requisito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; // Ej: Revision Tecnica, Casco
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscripcion_id")
    @JsonIgnore
    @ToString.Exclude
    private Inscripcion inscripcion;
}
