package com.carmeet.ms_notification_log.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notificacion")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notificacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String destinatario;
    private String mensaje;
}
