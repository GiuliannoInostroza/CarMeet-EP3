package com.carmeet.ms_notification_log.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "adjunto")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Adjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreArchivo; 
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacion_id")
    @JsonIgnore
    @ToString.Exclude
    private Notificacion notificacion;
}
