package com.carmeet.ms_vehicle_registry.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehiculo")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehiculo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String patente;
    private String marca;
    private String modelo;
    private String username;
}
