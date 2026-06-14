package com.carmeet.ms_notification_log.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "notificacion")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notificacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String destinatario;
    private String mensaje;

    @Column(name = "leida", nullable = false)
    @Builder.Default
    private Boolean leida = false;

    @OneToMany(mappedBy = "notificacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Adjunto> adjuntos = new ArrayList<>();
}
