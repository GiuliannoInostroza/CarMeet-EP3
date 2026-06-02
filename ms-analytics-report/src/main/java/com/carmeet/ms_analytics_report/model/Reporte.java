package com.carmeet.ms_analytics_report.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "reporte")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Reporte {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventoId;
    private Integer totalEventos;
    private Integer totalTickets;
    private Integer totalInscripciones;
    private String fechaGeneracion;

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Metrica> metricas = new ArrayList<>();
}
