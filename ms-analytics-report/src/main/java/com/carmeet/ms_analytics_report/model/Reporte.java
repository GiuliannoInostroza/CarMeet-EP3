package com.carmeet.ms_analytics_report.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reporte")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Reporte {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer totalEventos;
    private String fechaGeneracion;
}
