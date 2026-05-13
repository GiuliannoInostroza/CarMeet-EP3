package com.carmeet.ms_vehicle_registry.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "vehiculo")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String marca;
    private String modelo;
    private Integer anio;

    @OneToMany(mappedBy = "vehiculo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Mantenimiento> mantenimientos = new ArrayList<>();
}
