package com.carmeet.ms_venue_capacity.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "recinto")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Recinto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer capacidadMaxima;
    private Integer ocupacionActual;

    @OneToMany(mappedBy = "recinto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Zona> zonas = new ArrayList<>();
}
