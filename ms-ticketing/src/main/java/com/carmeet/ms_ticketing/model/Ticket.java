package com.carmeet.ms_ticketing.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "ticket")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long eventoId;
    private Double precio;
    private String estado;
    private String username;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Beneficio> beneficios = new ArrayList<>();
}
