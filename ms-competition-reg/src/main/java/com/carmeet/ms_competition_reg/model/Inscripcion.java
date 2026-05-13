package com.carmeet.ms_competition_reg.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "inscripcion")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Inscripcion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long vehiculoId;
    private String categoria;
    private String username;

    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Requisito> requisitos = new ArrayList<>();
}
