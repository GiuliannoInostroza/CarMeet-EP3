package com.carmeet.ms_analytics_report.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "metrica")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Metrica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; // Ej: Total Asistentes, Ingresos
    private Double valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporte_id")
    @JsonIgnore
    @ToString.Exclude
    private Reporte reporte;
}
