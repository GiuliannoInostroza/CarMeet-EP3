package com.carmeet.ms_event_core.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "evento")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String fecha;
    private String ubicacion;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Patrocinador> patrocinadores = new ArrayList<>();
}
