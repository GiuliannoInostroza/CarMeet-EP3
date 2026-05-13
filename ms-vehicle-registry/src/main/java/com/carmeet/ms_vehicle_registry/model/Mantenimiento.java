package com.carmeet.ms_vehicle_registry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mantenimiento")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Mantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    @JsonIgnore
    @ToString.Exclude
    private Vehiculo vehiculo;
}
